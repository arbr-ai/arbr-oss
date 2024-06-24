package com.arbr.api_server_base.service.user

import com.arbr.api.github.core.*
import com.arbr.api.github.request.GitHubCreateUserRequest
import com.arbr.api.github.request.GitHubLoginRequest
import com.arbr.api.github.response.GitHubUserOAuthLoginResponse
import com.arbr.api.user.core.*
import com.arbr.api.user.request.*
import com.arbr.api.user_repos.response.GetUserRepoBranchesResponse
import com.arbr.api_server_base.service.github.client.GitHubClientFactory
import com.arbr.api_server_base.service.auth.CryptographicHasher
import com.arbr.api_server_base.service.auth.JwtBuilder
import com.arbr.api_server_base.service.auth.PrivateKeyKind
import com.arbr.api_server_base.service.github.GitHubCredentialsUtils.writeGitHubCredentials
import com.arbr.api_server_base.service.github.client.GitHubClient
import com.arbr.db.public.tables.pojos.UserAccount
import com.arbr.db.public.tables.pojos.UserAccountLinkGithub
import com.arbr.db.public.tables.references.USER_ACCOUNT
import com.arbr.util_common.reactor.switchIfEmptyContextual
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3
import reactor.kotlin.core.util.function.component4
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * User service with implementations for GitHub connections
 */
@Service
internal class GitHubUserService(
    private val cryptographicHasher: CryptographicHasher,
    private val gitHubUserAccountRepository: GitHubUserAccountRepository,
    private val userAccountRepository: UserAccountRepository,
    private val userEmailRepository: UserEmailRepository,
    private val gitHubClientFactory: GitHubClientFactory,
) : UserService {
    private data class GitHubLoggedInModel(
        val userDetail: GitHubUserDetail,
        val encryptedToken: String,
    )

    private val userCreateCache = ConcurrentHashMap<String, Mono<UserWithCredentials>>()

    @Scheduled(fixedDelay = 30_000)
    fun clearCache() {
        userCreateCache.clear()
    }

    private fun getGitHubUserDetail(
        client: GitHubClient,
    ): Mono<GitHubUserDetail> {
        return Mono.zip(
            client.getUser(),
            client.getUserRepos(),
            client.getUserOrganizations(),
            // Fetch emails but don't error out if we fail
            client
                .getUserEmails()
                .onErrorReturn(emptyList())
                .defaultIfEmpty(emptyList()),
        ).map { (userInfo, repoDetails, organizationInfos, emails) ->
            GitHubUserDetail(
                userInfo.id,
                userInfo.login,
                userInfo.avatarUrl,
                emails,
                organizationInfos,
                repoDetails,
            )
        }
    }

    private fun getGitHubClient(
        accessToken: String
    ): GitHubClient {
        return gitHubClientFactory.client(accessToken)
    }

    private fun getGitHubClient(
        userAccountGitHubLink: UserAccountLinkGithub
    ): GitHubClient {
        val accessToken = cryptographicHasher.decrypt(
            PrivateKeyKind.GITHUB_ACCESS_TOKEN,
            userAccountGitHubLink.githubKey!!
        )

        return getGitHubClient(accessToken)
    }

    private fun getGitHubClient(
        gitHubUserOAuthLoginResponse: GitHubUserOAuthLoginResponse,
    ): GitHubClient {
        return getGitHubClient(gitHubUserOAuthLoginResponse.accessToken)
    }

    private fun getUserInfo(
        gitHubUserDetail: GitHubUserDetail?,
        userAccount: UserAccount,
    ): UserInfo {
        return UserInfo(
            id = userAccount.id!!,
            username = userAccount.username,
            avatarUrl = userAccount.avatarUrl ?: gitHubUserDetail?.avatarUrl,
            gitHub = gitHubUserDetail,
        )
    }

    private fun getUserInfo(
        userAccount: UserAccount,
    ): Mono<UserInfo> {
        return gitHubUserAccountRepository
            .getUserAccountGitHubLinkByUserId(userAccount.id!!)
            .map(this::getGitHubClient)
            .flatMap(this::getGitHubUserDetail)
            .map { gitHubUserDetail ->
                getUserInfo(gitHubUserDetail, userAccount)
            }
            .switchIfEmpty {
                Mono.just(getUserInfo(null, userAccount))
            }
    }

    private fun linkGitHubAccountToDbUser(
        userId: Long,
        username: String,
        loggedInModel: GitHubLoggedInModel,
        appName: String,
    ): Mono<Void> {
        val creationTimestamp = Instant.now().toEpochMilli()
        val userModel = loggedInModel.userDetail
        val userEmails = userModel.emails

        val gitHubId = userModel.id.toString()
        val gitHubKey = loggedInModel.encryptedToken

        return gitHubUserAccountRepository
            .attachGitHubAccountToUserAccount(
                userId,
                username,
                creationTimestamp,
                gitHubId,
                gitHubKey,
                appName,
            )
            .then(
                Flux.fromIterable(userEmails)
                    .concatMap { userEmail ->
                        userEmailRepository
                            .addEmailToUser(
                                userId,
                                creationTimestamp,
                                creationTimestamp,
                                userEmail.email,
                                UserEmailSource.GITHUB.serializedValue,
                                userEmail.verified,
                                userEmail.primary,
                            )
                    }
                    .collectList()
            )
            .then()
    }

    private fun generateGitHubUserPassword(
        username: String,
        githubId: String,
    ): String = "$pkeySalt gh-$username ghid-$githubId"

    private fun createOrUpdateDbUser(
        loggedInModel: GitHubLoggedInModel,
        appName: String,
        jwtBuilder: JwtBuilder.WithKind,
    ): Mono<UserWithCredentials> {
        val userModel = loggedInModel.userDetail
        val encryptedToken = loggedInModel.encryptedToken
        val userEmails = userModel.emails

        val username = userModel.login
            ?: "ghfbu-${UUID.randomUUID()}"

        val gitHubId = userModel.id.toString()
        val rawPassword = generateGitHubUserPassword(username, gitHubId)
        val passwordKey = cryptographicHasher.encrypt(
            PrivateKeyKind.GITHUB_ACCESS_TOKEN,
            rawPassword
        )
        val creationTimestamp = Instant.now().toEpochMilli()

        return gitHubUserAccountRepository
            .getUserAccountGitHubLinkByUsername(username)
            .flatMap {
                val userId = it.userId
                if (it.githubKey == encryptedToken) {
                    Mono.empty()
                } else {
                    gitHubUserAccountRepository.update(
                        userId, encryptedToken
                    )
                }
                    .thenReturn(
                        UserInfo(
                            userId,
                            username,
                            userModel.avatarUrl,
                            userModel,
                        )
                    )
            }
            .switchIfEmptyContextual {
                gitHubUserAccountRepository.create(
                    creationTimestamp,
                    username,
                    passwordKey,
                    gitHubId,
                    encryptedToken,
                    UserRole.toBitmask(newUserRoles),
                    userModel.avatarUrl,
                    appName,
                ).map {
                    UserInfo(
                        it.userId,
                        username,
                        userModel.avatarUrl,
                        userModel,
                    )
                }
            }
            .flatMap { userInfo ->
                Flux.fromIterable(userEmails)
                    .concatMap { userEmail ->
                        userEmailRepository
                            .addEmailToUser(
                                userInfo.id,
                                creationTimestamp,
                                creationTimestamp,
                                userEmail.email,
                                UserEmailSource.GITHUB.serializedValue,
                                userEmail.verified,
                                userEmail.primary,
                            )
                    }
                    .collectList()
                    .thenReturn(userInfo)
            }
            .map { userInfo ->
                val jwt = jwtBuilder.withSubjectClaims(
                    userInfo.id,
                    userInfo.username,
                    newUserRoles,
                ).build()
                UserWithCredentials(
                    userInfo,
                    jwt,
                )
            }
    }

    private fun logIntoGitHubWithAppCode(
        code: String,
        appName: String,
    ): Mono<GitHubLoggedInModel> {
        return gitHubClientFactory
            .loginClient()
            .loginWithOAuthCode(code, appName)
            .flatMap { gitHubLoginResponse ->
                val encryptedToken = cryptographicHasher
                    .encrypt(PrivateKeyKind.GITHUB_ACCESS_TOKEN, gitHubLoginResponse.accessToken)

                val client = getGitHubClient(gitHubLoginResponse)
                getGitHubUserDetail(client).map {
                    GitHubLoggedInModel(it, encryptedToken)
                }
            }
    }

    private fun encryptUserToken(rawPassword: String): String {
        return cryptographicHasher.encrypt(PrivateKeyKind.USER_TOKEN, rawPassword)
    }

    private fun loginWithUserId(
        userId: Long,
        password: String,
    ): Mono<UserInfo> {
        val passwordKey = encryptUserToken(password)
        return userAccountRepository.findById(userId)
            .filter {
                passwordKey == it.passwordKey
            }
            .flatMap(this::getUserInfo)
    }

    private fun loginWithUsername(
        username: String,
        password: String,
    ): Mono<UserInfo> {
        val passwordKey = encryptUserToken(password)
        return userAccountRepository.findByUsername(username)
            .filter {
                passwordKey == it.passwordKey
            }
            .flatMap(this::getUserInfo)
    }

    private fun loginWithEmail(
        email: String,
        password: String,
    ): Mono<UserInfo> {
        val passwordKey = encryptUserToken(password)
        return userAccountRepository.findByEmail(email)
            .filter {
                it.passwordKey == passwordKey
            }
            .flatMap(this::getUserInfo)
    }

    private fun logInByCredentials(
        loginRequest: CredentialsBasedLoginRequest,
        jwtBuilder: JwtBuilder.WithKind,
    ): Mono<UserWithCredentials> {
        val username = loginRequest.username
        val userId = loginRequest.userId
        val email = loginRequest.email
        val password = loginRequest.password

        val creds = listOfNotNull(username, userId, email)

        val userInfoMono = if (creds.size > 1) {
            Mono.error(IllegalArgumentException("Exactly one of username, userId, email required"))
        } else if (userId != null) {
            loginWithUserId(userId, password)
        } else if (username != null) {
            loginWithUsername(username, password)
        } else if (email != null) {
            loginWithEmail(email, password)
        } else {
            Mono.error(IllegalArgumentException("One of username, userId, password required"))
        }

        return userInfoMono
            .map { userInfo ->
                val jwt = jwtBuilder.withSubjectClaims(
                    userInfo.id,
                    userInfo.username,
                    newUserRoles,
                ).build()
                UserWithCredentials(
                    userInfo,
                    jwt,
                )
            }
    }

    private fun logInByGitHub(
        gitHubLoginRequest: GitHubLoginRequest,
        jwtBuilder: JwtBuilder.WithKind,
    ): Mono<UserWithCredentials> {
        return logIntoGitHubWithAppCode(
            gitHubLoginRequest.code,
            gitHubLoginRequest.appName,
        ).flatMap { loggedInModel ->
            val gitHubUserDetail = loggedInModel.userDetail

            // TODO: Simplify
            val gitHubId = gitHubUserDetail.id.toString()
            gitHubUserAccountRepository
                .getUserAccountGitHubLinkByGitHubId(gitHubId)
                .flatMap { accountLink ->
                    userAccountRepository.findById(accountLink.userId)
                        .map { userAccount ->
                            val userInfo = getUserInfo(
                                gitHubUserDetail,
                                userAccount,
                            )
                            val jwt = jwtBuilder.withSubjectClaims(
                                userInfo.id,
                                userInfo.username,
                                newUserRoles,
                            ).build()

                            UserWithCredentials(
                                userInfo,
                                jwt
                            )
                        }
                }
        }
    }

    private fun createUserByCredentials(
        createUserRequest: CredentialsBasedCreateUserRequest,
        jwtBuilder: JwtBuilder.WithKind,
    ): Mono<UserWithCredentials> {
        val username = createUserRequest.username
        val email = createUserRequest.email
        val password = createUserRequest.password

        if (username == null && email == null) {
            return Mono.error(IllegalArgumentException("Username or password required"))
        }

        val creationTimestamp = Instant.now().toEpochMilli()
        val passwordKey = cryptographicHasher.encrypt(
            PrivateKeyKind.USER_TOKEN,
            password,
        )

        return userAccountRepository
            .create(
                creationTimestamp,
                username,
                email,
                passwordKey,
                UserRole.toBitmask(newUserRoles),
            )
            .flatMap { newUserAccount ->
                // Write new email to DB
                val newEmailAddress = newUserAccount.email
                if (newEmailAddress == null) {
                    Mono.empty()
                } else {
                    userEmailRepository.addEmailToUser(
                        newUserAccount.id!!,
                        creationTimestamp,
                        creationTimestamp,
                        newEmailAddress,
                        UserEmailSource.FIRST_PARTY.serializedValue,
                        ghVerified = null,
                        ghPrimary = null,
                    )
                        .then()
                }.thenReturn(newUserAccount)
            }
            .flatMap(this::getUserInfo)
            .map { userInfo ->
                val jwt = jwtBuilder.withSubjectClaims(
                    userInfo.id,
                    userInfo.username,
                    newUserRoles,
                ).build()
                UserWithCredentials(
                    userInfo,
                    jwt,
                )
            }
    }

    /**
     * Create a user based on the given GitHub credentials if one does not already exist. If it does, log in to that
     * user account.
     */
    private fun createUserByGitHub(
        gitHubCreateUserRequest: GitHubCreateUserRequest,
        jwtBuilder: JwtBuilder.WithKind,
    ): Mono<UserWithCredentials> {
        return userCreateCache.computeIfAbsent(gitHubCreateUserRequest.code) { code ->
            Mono.defer {
                logIntoGitHubWithAppCode(
                    code,
                    gitHubCreateUserRequest.appName,
                ).flatMap { loggedInModel ->
                    createOrUpdateDbUser(
                        loggedInModel,
                        gitHubCreateUserRequest.appName,
                        jwtBuilder,
                    )
                }
            }.cache(
                /* ttlForValue = */
                { Duration.ofMinutes(1L) }, // Cache values for a reasonably long time
                /* ttlForError = */
                { Duration.ZERO }, // Do not cache errors
                /* ttlForEmpty = */
                { Duration.ZERO }, // Do not cache empty
            )
        }
    }

    private fun linkGitHubAccountToCreatedUser(
        userInfo: UserInfo,
        code: String,
        appName: String,
    ): Mono<UserInfo> {
        val username = userInfo.username!!

        return logIntoGitHubWithAppCode(
            code,
            appName,
        ).flatMap { loggedInModel ->
            linkGitHubAccountToDbUser(
                userInfo.id,
                username,
                loggedInModel,
                appName,
            )
        }
            .then(
                Mono.defer {
                    // Get a fresh user after linking to avoid any logic drift
                    getUserById(userInfo.id)
                }
            )
    }

    /**
     * Public interface
     */

    override fun getUserById(
        id: Long,
    ): Mono<UserInfo> {
        return userAccountRepository.findById(id)
            .flatMap(this::getUserInfo)
    }

    override fun getUserByUsername(username: String): Mono<UserInfo> {
        return userAccountRepository.findByUsername(username)
            .flatMap(this::getUserInfo)
    }

    private fun getUserGitHubAccessToken(
        username: String,
    ): Mono<String> {
        return gitHubUserAccountRepository.getUserAccountGitHubLinkByUsername(username)
            .mapNotNull<String> { it.githubKey }
            .map {
                cryptographicHasher.decrypt(PrivateKeyKind.GITHUB_ACCESS_TOKEN, it)
            }
    }

    override fun getUserCredentials(
        userId: Long,
    ): Mono<UserWorkflowCredentials> {
        return getUserById(userId)
            .flatMap { user ->
                val username = user.username!!
                getUserGitHubAccessToken(username)
                    .flatMap { accessToken ->
                        val client = getGitHubClient(accessToken)
                        getGitHubUserDetail(client)
                            .map { gitHubUserDetail ->
                                UserWorkflowCredentials(
                                    null, // TODO: Get JWT from context
                                    UserGitHubCredentials(gitHubUserDetail.login!!, accessToken),
                                )
                            }
                    }

            }
    }

    override fun getUserPasswordKey(id: Long): Mono<String> {
        return userAccountRepository.findById(id)
            .map {
                it.passwordKey
            }
    }

    override fun createUser(
        createUserRequest: CreateUserRequest,
        jwtBuilder: JwtBuilder,
    ): Mono<UserWithCredentials> {
        val credentials = createUserRequest.credentials
        val gitHubCreateUserRequest = createUserRequest.gitHub

        return if (credentials != null) {
            val createUserMono = createUserByCredentials(
                credentials,
                jwtBuilder.withKind(PrivateKeyKind.USER_TOKEN),
            )

            if (gitHubCreateUserRequest == null) {
                createUserMono
            } else {
                // Create user and attach gitHub credentials
                createUserMono
                    .flatMap { userWithCredentials ->
                        linkGitHubAccountToCreatedUser(
                            userWithCredentials.user,
                            gitHubCreateUserRequest.code,
                            gitHubCreateUserRequest.appName,
                        ).map { updatedUserInfo ->
                            userWithCredentials.copy(
                                user = updatedUserInfo,
                            )
                        }
                    }
            }
        } else if (gitHubCreateUserRequest != null) {
            createUserByGitHub(
                gitHubCreateUserRequest,
                jwtBuilder.withKind(PrivateKeyKind.GITHUB_ACCESS_TOKEN)
            )
        } else {
            Mono.error(IllegalArgumentException("One of credentials, gitHub required"))
        }
    }

    override fun logIn(
        loginRequest: LoginRequest,
        jwtBuilder: JwtBuilder,
    ): Mono<UserWithCredentials> {
        val credentials = loginRequest.credentials
        val gitHubLoginRequest = loginRequest.gitHub

        return if (credentials != null) {
            logInByCredentials(
                credentials,
                jwtBuilder.withKind(PrivateKeyKind.USER_TOKEN),
            )
        } else if (gitHubLoginRequest != null) {
            logInByGitHub(
                gitHubLoginRequest,
                jwtBuilder.withKind(PrivateKeyKind.GITHUB_ACCESS_TOKEN),
            )
        } else {
            Mono.error(IllegalArgumentException("One of credentials, gitHub required"))
        }
    }

    override fun createGitHubConnection(
        userId: Long,
        gitHubLoginRequest: GitHubLoginRequest
    ): Mono<UserInfo> {
        return getUserById(userId)
            .flatMap { userInfo ->
                linkGitHubAccountToCreatedUser(
                    userInfo,
                    gitHubLoginRequest.code,
                    gitHubLoginRequest.appName,
                )
            }
    }

    override fun getUserRepoBranches(
        username: String,
        owner: String,
        repoShortName: String,
    ): Mono<GetUserRepoBranchesResponse> {
        return gitHubUserAccountRepository.getUserAccountGitHubLinkByUsername(username)
            .flatMap { userAccountGitHubLink ->
                val client = getGitHubClient(userAccountGitHubLink)
                client.getRepoBranches(owner, repoShortName)
            }
    }

    override fun getUserOrganizationRepos(
        username: String,
        organization: String,
    ): Mono<List<GitHubRepoInfo>> {
        return gitHubUserAccountRepository.getUserAccountGitHubLinkByUsername(username)
            .flatMap { userAccountGitHubLink ->
                val client = getGitHubClient(userAccountGitHubLink)
                client.getOrganization(organization)
                    .flatMap {
                        client.getUserOrganizationRepos(
                            it.reposUrl,
                        )
                    }
            }
    }

    override fun updateUserById(
        id: Long,
        updateUserRequest: UpdateUserRequest,
    ): Mono<UserInfo> {
        return userAccountRepository.update(id) { userAccountRecord ->
            userAccountRecord.set(USER_ACCOUNT.USERNAME, updateUserRequest.username)
        }.flatMap(this::getUserInfo)
    }

    override fun deleteUserById(id: Long): Mono<Void> {
        return userAccountRepository.deleteById(id)
    }

    companion object {
        private const val pkeySalt = 67123L

        private val newUserRoles = listOf(
            UserRole.USER,
        )
    }
}
