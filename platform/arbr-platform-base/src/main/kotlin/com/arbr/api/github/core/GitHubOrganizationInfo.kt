package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * GitHub Organization info from API
 * "organization": {
 *       "login": "topdown-ai",
 *       "id": 128931680,
 *       "node_id": "O_kgDOB69XYA",
 *       "url": "https://api.github.com/orgs/topdown-ai",
 *       "repos_url": "https://api.github.com/orgs/topdown-ai/repos",
 *       "events_url": "https://api.github.com/orgs/topdown-ai/events",
 *       "hooks_url": "https://api.github.com/orgs/topdown-ai/hooks",
 *       "issues_url": "https://api.github.com/orgs/topdown-ai/issues",
 *       "members_url": "https://api.github.com/orgs/topdown-ai/members{/member}",
 *       "public_members_url": "https://api.github.com/orgs/topdown-ai/public_members{/member}",
 *       "avatar_url": "https://avatars.githubusercontent.com/u/128931680?v=4",
 *       "description": null
 *     }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubOrganizationInfo(
    val login: String,
    val reposUrl: String,
    val avatarUrl: String,
)
