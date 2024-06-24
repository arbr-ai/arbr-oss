package com.arbr.api_server_base.service.auth

data class PrivateKeys(
    val userAuth: String,
    val githubAuth: String,
) {

    fun forKind(kind: PrivateKeyKind): String {
        return when (kind) {
            PrivateKeyKind.USER_TOKEN -> userAuth
            PrivateKeyKind.GITHUB_ACCESS_TOKEN ->
                githubAuth
        }
    }
}
