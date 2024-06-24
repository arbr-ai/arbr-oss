/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables.pojos


import java.io.Serializable


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class GithubRepo(
    val id: Long,
    val nodeId: String,
    val name: String,
    val fullName: String,
    val `private`: Boolean,
    val ownerLogin: String,
    val htmlUrl: String,
    val description: String? = null,
    val fork: Boolean,
    val url: String,
    val forksUrl: String,
    val keysUrl: String,
    val collaboratorsUrl: String,
    val teamsUrl: String,
    val hooksUrl: String,
    val issueEventsUrl: String,
    val eventsUrl: String,
    val assigneesUrl: String,
    val branchesUrl: String,
    val tagsUrl: String,
    val blobsUrl: String,
    val gitTagsUrl: String,
    val gitRefsUrl: String,
    val treesUrl: String,
    val statusesUrl: String,
    val languagesUrl: String,
    val stargazersUrl: String,
    val contributorsUrl: String,
    val subscribersUrl: String,
    val subscriptionUrl: String,
    val commitsUrl: String,
    val gitCommitsUrl: String,
    val commentsUrl: String,
    val issueCommentUrl: String,
    val contentsUrl: String,
    val compareUrl: String,
    val mergesUrl: String,
    val archiveUrl: String,
    val downloadsUrl: String,
    val issuesUrl: String,
    val pullsUrl: String,
    val milestonesUrl: String,
    val notificationsUrl: String,
    val labelsUrl: String,
    val releasesUrl: String,
    val deploymentsUrl: String,
    val createdAt: String,
    val updatedAt: String,
    val pushedAt: String,
    val gitUrl: String,
    val sshUrl: String,
    val cloneUrl: String,
    val svnUrl: String,
    val homepage: String? = null,
    val size: Int,
    val stargazersCount: Int,
    val watchersCount: Int,
    val language: String? = null,
    val hasIssues: Boolean,
    val hasProjects: Boolean,
    val hasDownloads: Boolean,
    val hasWiki: Boolean,
    val hasPages: Boolean,
    val hasDiscussions: Boolean,
    val forksCount: Int,
    val mirrorUrl: String? = null,
    val archived: Boolean,
    val disabled: Boolean,
    val openIssuesCount: Int,
    val license: String? = null,
    val allowForking: Boolean,
    val isTemplate: Boolean,
    val webCommitSignoffRequired: Boolean,
    val visibility: String,
    val forks: Int,
    val openIssues: Int,
    val watchers: Int,
    val defaultBranch: String,
    val tempCloneToken: String? = null,
    val networkCount: Int,
    val subscribersCount: Int
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: GithubRepo = other as GithubRepo
        if (this.id != o.id)
            return false
        if (this.nodeId != o.nodeId)
            return false
        if (this.name != o.name)
            return false
        if (this.fullName != o.fullName)
            return false
        if (this.`private` != o.`private`)
            return false
        if (this.ownerLogin != o.ownerLogin)
            return false
        if (this.htmlUrl != o.htmlUrl)
            return false
        if (this.description == null) {
            if (o.description != null)
                return false
        }
        else if (this.description != o.description)
            return false
        if (this.fork != o.fork)
            return false
        if (this.url != o.url)
            return false
        if (this.forksUrl != o.forksUrl)
            return false
        if (this.keysUrl != o.keysUrl)
            return false
        if (this.collaboratorsUrl != o.collaboratorsUrl)
            return false
        if (this.teamsUrl != o.teamsUrl)
            return false
        if (this.hooksUrl != o.hooksUrl)
            return false
        if (this.issueEventsUrl != o.issueEventsUrl)
            return false
        if (this.eventsUrl != o.eventsUrl)
            return false
        if (this.assigneesUrl != o.assigneesUrl)
            return false
        if (this.branchesUrl != o.branchesUrl)
            return false
        if (this.tagsUrl != o.tagsUrl)
            return false
        if (this.blobsUrl != o.blobsUrl)
            return false
        if (this.gitTagsUrl != o.gitTagsUrl)
            return false
        if (this.gitRefsUrl != o.gitRefsUrl)
            return false
        if (this.treesUrl != o.treesUrl)
            return false
        if (this.statusesUrl != o.statusesUrl)
            return false
        if (this.languagesUrl != o.languagesUrl)
            return false
        if (this.stargazersUrl != o.stargazersUrl)
            return false
        if (this.contributorsUrl != o.contributorsUrl)
            return false
        if (this.subscribersUrl != o.subscribersUrl)
            return false
        if (this.subscriptionUrl != o.subscriptionUrl)
            return false
        if (this.commitsUrl != o.commitsUrl)
            return false
        if (this.gitCommitsUrl != o.gitCommitsUrl)
            return false
        if (this.commentsUrl != o.commentsUrl)
            return false
        if (this.issueCommentUrl != o.issueCommentUrl)
            return false
        if (this.contentsUrl != o.contentsUrl)
            return false
        if (this.compareUrl != o.compareUrl)
            return false
        if (this.mergesUrl != o.mergesUrl)
            return false
        if (this.archiveUrl != o.archiveUrl)
            return false
        if (this.downloadsUrl != o.downloadsUrl)
            return false
        if (this.issuesUrl != o.issuesUrl)
            return false
        if (this.pullsUrl != o.pullsUrl)
            return false
        if (this.milestonesUrl != o.milestonesUrl)
            return false
        if (this.notificationsUrl != o.notificationsUrl)
            return false
        if (this.labelsUrl != o.labelsUrl)
            return false
        if (this.releasesUrl != o.releasesUrl)
            return false
        if (this.deploymentsUrl != o.deploymentsUrl)
            return false
        if (this.createdAt != o.createdAt)
            return false
        if (this.updatedAt != o.updatedAt)
            return false
        if (this.pushedAt != o.pushedAt)
            return false
        if (this.gitUrl != o.gitUrl)
            return false
        if (this.sshUrl != o.sshUrl)
            return false
        if (this.cloneUrl != o.cloneUrl)
            return false
        if (this.svnUrl != o.svnUrl)
            return false
        if (this.homepage == null) {
            if (o.homepage != null)
                return false
        }
        else if (this.homepage != o.homepage)
            return false
        if (this.size != o.size)
            return false
        if (this.stargazersCount != o.stargazersCount)
            return false
        if (this.watchersCount != o.watchersCount)
            return false
        if (this.language == null) {
            if (o.language != null)
                return false
        }
        else if (this.language != o.language)
            return false
        if (this.hasIssues != o.hasIssues)
            return false
        if (this.hasProjects != o.hasProjects)
            return false
        if (this.hasDownloads != o.hasDownloads)
            return false
        if (this.hasWiki != o.hasWiki)
            return false
        if (this.hasPages != o.hasPages)
            return false
        if (this.hasDiscussions != o.hasDiscussions)
            return false
        if (this.forksCount != o.forksCount)
            return false
        if (this.mirrorUrl == null) {
            if (o.mirrorUrl != null)
                return false
        }
        else if (this.mirrorUrl != o.mirrorUrl)
            return false
        if (this.archived != o.archived)
            return false
        if (this.disabled != o.disabled)
            return false
        if (this.openIssuesCount != o.openIssuesCount)
            return false
        if (this.license == null) {
            if (o.license != null)
                return false
        }
        else if (this.license != o.license)
            return false
        if (this.allowForking != o.allowForking)
            return false
        if (this.isTemplate != o.isTemplate)
            return false
        if (this.webCommitSignoffRequired != o.webCommitSignoffRequired)
            return false
        if (this.visibility != o.visibility)
            return false
        if (this.forks != o.forks)
            return false
        if (this.openIssues != o.openIssues)
            return false
        if (this.watchers != o.watchers)
            return false
        if (this.defaultBranch != o.defaultBranch)
            return false
        if (this.tempCloneToken == null) {
            if (o.tempCloneToken != null)
                return false
        }
        else if (this.tempCloneToken != o.tempCloneToken)
            return false
        if (this.networkCount != o.networkCount)
            return false
        if (this.subscribersCount != o.subscribersCount)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + this.id.hashCode()
        result = prime * result + this.nodeId.hashCode()
        result = prime * result + this.name.hashCode()
        result = prime * result + this.fullName.hashCode()
        result = prime * result + this.`private`.hashCode()
        result = prime * result + this.ownerLogin.hashCode()
        result = prime * result + this.htmlUrl.hashCode()
        result = prime * result + (if (this.description == null) 0 else this.description.hashCode())
        result = prime * result + this.fork.hashCode()
        result = prime * result + this.url.hashCode()
        result = prime * result + this.forksUrl.hashCode()
        result = prime * result + this.keysUrl.hashCode()
        result = prime * result + this.collaboratorsUrl.hashCode()
        result = prime * result + this.teamsUrl.hashCode()
        result = prime * result + this.hooksUrl.hashCode()
        result = prime * result + this.issueEventsUrl.hashCode()
        result = prime * result + this.eventsUrl.hashCode()
        result = prime * result + this.assigneesUrl.hashCode()
        result = prime * result + this.branchesUrl.hashCode()
        result = prime * result + this.tagsUrl.hashCode()
        result = prime * result + this.blobsUrl.hashCode()
        result = prime * result + this.gitTagsUrl.hashCode()
        result = prime * result + this.gitRefsUrl.hashCode()
        result = prime * result + this.treesUrl.hashCode()
        result = prime * result + this.statusesUrl.hashCode()
        result = prime * result + this.languagesUrl.hashCode()
        result = prime * result + this.stargazersUrl.hashCode()
        result = prime * result + this.contributorsUrl.hashCode()
        result = prime * result + this.subscribersUrl.hashCode()
        result = prime * result + this.subscriptionUrl.hashCode()
        result = prime * result + this.commitsUrl.hashCode()
        result = prime * result + this.gitCommitsUrl.hashCode()
        result = prime * result + this.commentsUrl.hashCode()
        result = prime * result + this.issueCommentUrl.hashCode()
        result = prime * result + this.contentsUrl.hashCode()
        result = prime * result + this.compareUrl.hashCode()
        result = prime * result + this.mergesUrl.hashCode()
        result = prime * result + this.archiveUrl.hashCode()
        result = prime * result + this.downloadsUrl.hashCode()
        result = prime * result + this.issuesUrl.hashCode()
        result = prime * result + this.pullsUrl.hashCode()
        result = prime * result + this.milestonesUrl.hashCode()
        result = prime * result + this.notificationsUrl.hashCode()
        result = prime * result + this.labelsUrl.hashCode()
        result = prime * result + this.releasesUrl.hashCode()
        result = prime * result + this.deploymentsUrl.hashCode()
        result = prime * result + this.createdAt.hashCode()
        result = prime * result + this.updatedAt.hashCode()
        result = prime * result + this.pushedAt.hashCode()
        result = prime * result + this.gitUrl.hashCode()
        result = prime * result + this.sshUrl.hashCode()
        result = prime * result + this.cloneUrl.hashCode()
        result = prime * result + this.svnUrl.hashCode()
        result = prime * result + (if (this.homepage == null) 0 else this.homepage.hashCode())
        result = prime * result + this.size.hashCode()
        result = prime * result + this.stargazersCount.hashCode()
        result = prime * result + this.watchersCount.hashCode()
        result = prime * result + (if (this.language == null) 0 else this.language.hashCode())
        result = prime * result + this.hasIssues.hashCode()
        result = prime * result + this.hasProjects.hashCode()
        result = prime * result + this.hasDownloads.hashCode()
        result = prime * result + this.hasWiki.hashCode()
        result = prime * result + this.hasPages.hashCode()
        result = prime * result + this.hasDiscussions.hashCode()
        result = prime * result + this.forksCount.hashCode()
        result = prime * result + (if (this.mirrorUrl == null) 0 else this.mirrorUrl.hashCode())
        result = prime * result + this.archived.hashCode()
        result = prime * result + this.disabled.hashCode()
        result = prime * result + this.openIssuesCount.hashCode()
        result = prime * result + (if (this.license == null) 0 else this.license.hashCode())
        result = prime * result + this.allowForking.hashCode()
        result = prime * result + this.isTemplate.hashCode()
        result = prime * result + this.webCommitSignoffRequired.hashCode()
        result = prime * result + this.visibility.hashCode()
        result = prime * result + this.forks.hashCode()
        result = prime * result + this.openIssues.hashCode()
        result = prime * result + this.watchers.hashCode()
        result = prime * result + this.defaultBranch.hashCode()
        result = prime * result + (if (this.tempCloneToken == null) 0 else this.tempCloneToken.hashCode())
        result = prime * result + this.networkCount.hashCode()
        result = prime * result + this.subscribersCount.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("GithubRepo (")

        sb.append(id)
        sb.append(", ").append(nodeId)
        sb.append(", ").append(name)
        sb.append(", ").append(fullName)
        sb.append(", ").append(`private`)
        sb.append(", ").append(ownerLogin)
        sb.append(", ").append(htmlUrl)
        sb.append(", ").append(description)
        sb.append(", ").append(fork)
        sb.append(", ").append(url)
        sb.append(", ").append(forksUrl)
        sb.append(", ").append(keysUrl)
        sb.append(", ").append(collaboratorsUrl)
        sb.append(", ").append(teamsUrl)
        sb.append(", ").append(hooksUrl)
        sb.append(", ").append(issueEventsUrl)
        sb.append(", ").append(eventsUrl)
        sb.append(", ").append(assigneesUrl)
        sb.append(", ").append(branchesUrl)
        sb.append(", ").append(tagsUrl)
        sb.append(", ").append(blobsUrl)
        sb.append(", ").append(gitTagsUrl)
        sb.append(", ").append(gitRefsUrl)
        sb.append(", ").append(treesUrl)
        sb.append(", ").append(statusesUrl)
        sb.append(", ").append(languagesUrl)
        sb.append(", ").append(stargazersUrl)
        sb.append(", ").append(contributorsUrl)
        sb.append(", ").append(subscribersUrl)
        sb.append(", ").append(subscriptionUrl)
        sb.append(", ").append(commitsUrl)
        sb.append(", ").append(gitCommitsUrl)
        sb.append(", ").append(commentsUrl)
        sb.append(", ").append(issueCommentUrl)
        sb.append(", ").append(contentsUrl)
        sb.append(", ").append(compareUrl)
        sb.append(", ").append(mergesUrl)
        sb.append(", ").append(archiveUrl)
        sb.append(", ").append(downloadsUrl)
        sb.append(", ").append(issuesUrl)
        sb.append(", ").append(pullsUrl)
        sb.append(", ").append(milestonesUrl)
        sb.append(", ").append(notificationsUrl)
        sb.append(", ").append(labelsUrl)
        sb.append(", ").append(releasesUrl)
        sb.append(", ").append(deploymentsUrl)
        sb.append(", ").append(createdAt)
        sb.append(", ").append(updatedAt)
        sb.append(", ").append(pushedAt)
        sb.append(", ").append(gitUrl)
        sb.append(", ").append(sshUrl)
        sb.append(", ").append(cloneUrl)
        sb.append(", ").append(svnUrl)
        sb.append(", ").append(homepage)
        sb.append(", ").append(size)
        sb.append(", ").append(stargazersCount)
        sb.append(", ").append(watchersCount)
        sb.append(", ").append(language)
        sb.append(", ").append(hasIssues)
        sb.append(", ").append(hasProjects)
        sb.append(", ").append(hasDownloads)
        sb.append(", ").append(hasWiki)
        sb.append(", ").append(hasPages)
        sb.append(", ").append(hasDiscussions)
        sb.append(", ").append(forksCount)
        sb.append(", ").append(mirrorUrl)
        sb.append(", ").append(archived)
        sb.append(", ").append(disabled)
        sb.append(", ").append(openIssuesCount)
        sb.append(", ").append(license)
        sb.append(", ").append(allowForking)
        sb.append(", ").append(isTemplate)
        sb.append(", ").append(webCommitSignoffRequired)
        sb.append(", ").append(visibility)
        sb.append(", ").append(forks)
        sb.append(", ").append(openIssues)
        sb.append(", ").append(watchers)
        sb.append(", ").append(defaultBranch)
        sb.append(", ").append(tempCloneToken)
        sb.append(", ").append(networkCount)
        sb.append(", ").append(subscribersCount)

        sb.append(")")
        return sb.toString()
    }
}
