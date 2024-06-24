package com.arbr.api_server_base.service.user

import com.arbr.api.github.core.GitHubRepoInfo
import com.arbr.api.github.request.GitHubLoginRequest
import com.arbr.api.user.core.UserInfo
import com.arbr.api.user.core.UserWithCredentials
import com.arbr.api.user.request.CreateUserRequest
import com.arbr.api.user.request.LoginRequest
import com.arbr.api.user.request.UpdateUserRequest
import com.arbr.api.user_repos.response.GetUserRepoBranchesResponse
import com.arbr.api_server_base.service.auth.JwtBuilder
import reactor.core.publisher.Mono

interface UserService {
    fun getUserById(
        id: Long,
    ): Mono<UserInfo>

    fun getUserByUsername(
        username: String,
    ): Mono<UserInfo>

    fun getUserCredentials(
        userId: Long,
    ): Mono<UserWorkflowCredentials>

    fun getUserPasswordKey(
        id: Long,
    ): Mono<String>


    /**
     * Create a user based on the given credentials (1st-party or connection) if one does not already exist. If it
     * does, log in to that user account.
     */
    fun createUser(
        createUserRequest: CreateUserRequest,
        jwtBuilder: JwtBuilder,
    ): Mono<UserWithCredentials>

    fun logIn(
        loginRequest: LoginRequest,
        jwtBuilder: JwtBuilder,
    ): Mono<UserWithCredentials>

    /**
     * Attach a GitHub connection
     * TODO: Generalize connections
     */
    fun createGitHubConnection(
        userId: Long,
        gitHubLoginRequest: GitHubLoginRequest,
    ): Mono<UserInfo>

    fun getUserRepoBranches(
        username: String,
        owner: String,
        repoShortName: String,
    ): Mono<GetUserRepoBranchesResponse>

    fun getUserOrganizationRepos(
        username: String,
        organization: String,
    ): Mono<List<GitHubRepoInfo>>

    fun updateUserById(
        id: Long,
        updateUserRequest: UpdateUserRequest,
    ): Mono<UserInfo>

    fun deleteUserById(id: Long): Mono<Void>
}