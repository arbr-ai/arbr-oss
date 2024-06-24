package com.arbr.api_server_base.service.github.client

import com.arbr.api.github.core.GitHubApiError
import com.arbr.api.github.core.GitHubBranchSummary
import com.arbr.api.github.core.GitHubCreatePullRequestRequest
import com.arbr.api.github.core.GitHubCreateRepoRequest
import com.arbr.api.github.core.GitHubCreateRepoResult
import com.arbr.api.github.core.GitHubOrganizationInfo
import com.arbr.api.github.core.GitHubPullRequestInfo
import com.arbr.api.github.core.GitHubRepoDetail
import com.arbr.api.github.core.GitHubRepoInfo
import com.arbr.api.github.core.GitHubRepoSummary
import com.arbr.api.github.core.GitHubUserEmail
import com.arbr.api.github.core.GitHubUserInfo
import com.arbr.api.user_repos.response.GetUserRepoBranchesResponse
import com.arbr.util_common.reactor.single
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import java.net.URL
import java.time.Duration

class GitHubClient(
    private val mapper: ObjectMapper,
    private val personalAccessToken: String,
) {
    private val logger = LoggerFactory.getLogger(GitHubClient::class.java)

    private val retryPolicy = Retry
        .backoff(10L, Duration.ofSeconds(10L))
        .filter {
            it is WebClientResponseException.TooManyRequests
                    || it is RequestNotPermitted
                    || it is WebClientResponseException.Forbidden
                    || (it is WebClientResponseException && it.statusCode.is5xxServerError)
        }
        .doBeforeRetry {
            logger.info("Rate limited by GitHub (${it.totalRetries()}), waiting...")
        }

    private val rateLimiter: RateLimiter by lazy {
        val config = RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .limitForPeriod(80)
            .timeoutDuration(Duration.ofSeconds(90))
            .build()

        // Create registry
        val rateLimiterRegistry = RateLimiterRegistry.of(config)

        // Use registry
        rateLimiterRegistry.rateLimiter("github")
    }

    private val webClient: WebClient = run {
        val timeout = Duration.ofSeconds(120L)

        val httpClient = HttpClient.create()
            .responseTimeout(timeout)
            .resolver {
                // The initial DNS request to GitHub's API seems to always time out, so we need to set it to a very low
                // value
                // TODO: Add a request to warmup routine
                it.queryTimeout(Duration.ofSeconds(5L))
            }
            .doOnConnected { conn ->
                conn
                    .addHandlerFirst(ReadTimeoutHandler(timeout.toSeconds().toInt()))
                    .addHandlerFirst(WriteTimeoutHandler(timeout.toSeconds().toInt()))
            }

        val strategies = ExchangeStrategies.builder()
            .codecs { codecs: ClientCodecConfigurer ->
                codecs.defaultCodecs().maxInMemorySize(maxRequestSize)
                codecs.customCodecs().run {
                    register(CustomCodecs.snakeCaseDecoderHttpMessageReader())
                    register(CustomCodecs.snakeCaseEncoderHttpMessageWriter())
                }
            }
            .build()

        WebClient
            .builder()
            .baseUrl("https://api.github.com/")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.AUTHORIZATION, "token $personalAccessToken")
            .defaultHeader(HttpHeaders.ACCEPT, gitHubAcceptHeaderValue)
            .defaultHeader(apiVersionHeader, apiVersionHeaderValue)
            .filter { request, next ->
                next.exchange(request)
                    .doOnSubscribe {
                        logger.info("${request.method()} ${request.url()}")
                    }
                    .doOnNext { resp ->
                        logger.info("${resp.statusCode()} ${request.method()} ${request.url()}")
                    }
                    .doOnCancel {
                        logger.info("CANCEL ${request.method()} ${request.url()}")
                    }
                    .transformDeferred(RateLimiterOperator.of(rateLimiter))
            }
            .exchangeStrategies(strategies)
            .build()
    }

    private fun getPaginatedResourceByLink(link: String): Mono<List<*>> {
        val linkMap = Regex("<([^<>]*?)>; rel=\"(.*?)\"").findAll(link).toList().associate {
            it.groups[2]!!.value to it.groups[1]?.value
        }

        val fullUrl = linkMap["next"] ?: return Mono.just(emptyList<Any?>())

        return webClient
            .get()
            .uri(URL(fullUrl).file)
            .retrieve()
            .toEntity(ArrayList::class.java)
            .flatMap {
                val nextLink = it.headers["link"]?.get(0)

                if (nextLink == null) {
                    Mono.just(it.body ?: emptyList())
                } else {
                    getPaginatedResourceByLink(nextLink).map { nextList ->
                        (it.body ?: emptyList()) + nextList
                    }
                }
            }
            .retryWhen(retryPolicy)
    }

    private inline fun <reified T> getPaginatedResource(url: String): Mono<List<T>> {
        return webClient
            .get()
            .uri(url)
            .retrieve()
            .toEntity(ArrayList::class.java)
            .flatMap {
                val link = it.headers["link"]?.get(0)

                if (link == null) {
                    Mono.just(it.body ?: emptyList())
                } else {
                    getPaginatedResourceByLink(link).map { nextList ->
                        (it.body ?: emptyList()) + nextList
                    }
                }
            }
            .retryWhen(retryPolicy)
            .map { arrayList ->
                arrayList.map {
                    mapper.convertValue(it, T::class.java)
                }
            }
    }

    fun createUserRepo(
        name: String,
        description: String?,
        private: Boolean,
    ): Mono<GitHubCreateRepoResult> {
        val reposUrl = "user/repos"
        return createRepo(reposUrl, name, description, private)
    }

    fun createOrganizationRepo(
        organization: String,
        name: String,
        description: String?,
        private: Boolean,
    ): Mono<GitHubCreateRepoResult> {
        val reposUrl = "orgs/$organization/repos"
        return createRepo(reposUrl, name, description, private)
    }

    /**
     * https://docs.github.com/en/free-pro-team@latest/rest/repos/repos?apiVersion=2022-11-28#create-an-organization-repository
     *
     * Some parameters not included.
     */
    private fun createRepo(
        reposUrl: String,
        name: String,
        description: String?,
        private: Boolean,
    ): Mono<GitHubCreateRepoResult> {


        val visibility = if (private) {
            "private"
        } else {
            "public"
        }

        val createRepoRequest = GitHubCreateRepoRequest(
            name = name,
            description = description,
            private = private,
            visibility = visibility,
        )

        return webClient
            .post()
            .uri(reposUrl)
            .bodyValue(createRepoRequest)
            .retrieve()
            .bodyToMono(GitHubRepoSummary::class.java)
            .map<GitHubCreateRepoResult> {
                GitHubCreateRepoResult.Created(it)
            }
            .onErrorResume(WebClientResponseException::class.java) { ex ->
                val errorObject = mapper.readValue(ex.responseBodyAsString, GitHubApiError::class.java)

                if (ex.statusCode == HttpStatus.UNPROCESSABLE_ENTITY) {
                    if (errorObject.isExactly("Repository", "name already exists on this account")) {
                        Mono.just(GitHubCreateRepoResult.Exists)
                    } else {
                        Mono.error(ex)
                    }
                } else if (ex.statusCode == HttpStatus.FORBIDDEN) {
                    Mono.just(GitHubCreateRepoResult.Forbidden)
                } else {
                    Mono.error(ex)
                }
            }
            .retryWhen(retryPolicy)
            .doOnError(WebClientResponseException::class.java) {
                logger.info("Error from GH Create Repo")
                logger.info("Status:${it.statusCode}")
                logger.info("Error Body:\n${it.responseBodyAsString}")
                logger.info("====")
            }
    }

    fun getRepo(
        owner: String,
        repoShortName: String,
    ): Mono<GitHubRepoSummary> {
        val reposUrl = "repos/$owner/$repoShortName"

        // Note: could return detail here
        return webClient
            .get()
            .uri(reposUrl)
            .retrieve()
            .bodyToMono(GitHubRepoSummary::class.java)
            .retryWhen(retryPolicy)
            .doOnError(WebClientResponseException::class.java) {
                logger.info("Error from GH get Repo")
                logger.info("Status:${it.statusCode}")
                logger.info("Error Body:\n${it.responseBodyAsString}")
                logger.info("====")
            }
    }

    fun getRepoBranches(
        owner: String,
        repoShortName: String,
    ): Mono<GetUserRepoBranchesResponse> {
        val projectFullName = "$owner/$repoShortName"
        val url = "repos/$projectFullName/branches"

        return Mono.zip(
            getRepo(owner, repoShortName),
            getPaginatedResource<GitHubBranchSummary>(url)
        ).map { (repoSummary, branchSummaries) ->
            // TODO: Use an internal model instead of an API model here
            if (branchSummaries.isEmpty()) {
                GetUserRepoBranchesResponse(
                    repoSummary.defaultBranch,
                    listOf(
                        GitHubBranchSummary(repoSummary.defaultBranch)
                    ),
                )
            } else {
                GetUserRepoBranchesResponse(
                    repoSummary.defaultBranch,
                    branchSummaries
                )
            }
        }
    }

    /**
     * Get all repos the user has at least "explicit" read access to.
     *
     * Docs (https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28):
     * > The authenticated user has explicit permission to access repositories they own, repositories where they are
     * > a collaborator, and repositories that they can access through an organization membership.
     *
     * TODO: This blocks user account page loading, so consider aggressively prefetching pages
     */
    fun getUserRepos(): Mono<List<GitHubRepoDetail>> {
        val url = "user/repos"

        return getPaginatedResource<GitHubRepoDetail>(url)
            .single("No GitHub user repos")
    }

    fun getUserOrganizationRepos(
        reposUrl: String,
    ): Mono<List<GitHubRepoInfo>> {
        return webClient
            .get()
            .uri(reposUrl)
            .retrieve()
            .bodyToMono(ArrayList::class.java)
            .retryWhen(retryPolicy)
            .map { arrayList ->
                arrayList.map {
                    mapper.convertValue(it, GitHubRepoInfo::class.java)
                }
            }
    }

    fun getUserOrganizations(): Mono<List<GitHubOrganizationInfo>> {
        val url = "user/orgs"

        return webClient
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(ArrayList::class.java)
            .retryWhen(retryPolicy)
            .map { arrayList ->
                arrayList.map {
                    mapper.convertValue(it, GitHubOrganizationInfo::class.java)
                }
            }
            .single("No GitHub user orgs")
    }

    fun getOrganization(
        organization: String,
    ): Mono<GitHubOrganizationInfo> {
        val url = "orgs/$organization"

        return webClient
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(GitHubOrganizationInfo::class.java)
            .retryWhen(retryPolicy)
    }

    /**
     * User Profile Info
     */

    /**
     * Get logged-in user model
     */
    fun getUser(): Mono<GitHubUserInfo> {
        val url = "user"

        return webClient
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(GitHubUserInfo::class.java)
            .retryWhen(retryPolicy)
            .single("No GitHub user")
    }

    /**
     * Get user emails
     * https://docs.github.com/en/rest/users/emails?apiVersion=2022-11-28#list-email-addresses-for-the-authenticated-user
     * OAuth scope: `user:email`
     */
    fun getUserEmails(): Mono<List<GitHubUserEmail>> {
        val url = "user/emails"

        return webClient
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(ArrayList::class.java)
            .map { arrList ->
                arrList.map { elt ->
                    mapper.convertValue(elt, jacksonTypeRef<GitHubUserEmail>())
                }
            }
            .retryWhen(retryPolicy)
    }

    fun createPullRequest(
        repoFullName: String,
        title: String,
        head: String,
        base: String,
        body: String,
    ): Mono<GitHubPullRequestInfo> {
        val pullsUrl = "repos/$repoFullName/pulls"

        val req = GitHubCreatePullRequestRequest(
            title,
            head,
            base,
            body,
        )

        return webClient
            .post()
            .uri(pullsUrl)
            .bodyValue(req)
            .retrieve()
            .bodyToMono(GitHubPullRequestInfo::class.java)
            .retryWhen(retryPolicy)
            .doOnError(WebClientResponseException::class.java) {
                logger.info("Error from GH Create PR")
                logger.info("Status:${it.statusCode}")
                logger.info("Error Body:\n${it.responseBodyAsString}")
                logger.info("====")
            }
    }

    companion object {
        private const val maxRequestSize = 16 * 1024 * 1024

        private const val apiVersionHeader = "X-GitHub-Api-Version"
        private const val apiVersionHeaderValue = "2022-11-28"

        private const val gitHubAcceptHeaderValue = "application/vnd.github+json"
    }
}