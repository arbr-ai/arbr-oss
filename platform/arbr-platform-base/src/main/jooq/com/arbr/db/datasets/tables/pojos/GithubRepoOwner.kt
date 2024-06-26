/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables.pojos


import java.io.Serializable


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class GithubRepoOwner(
    val login: String,
    val id: Int,
    val nodeId: String,
    val avatarUrl: String,
    val gravatarId: String,
    val url: String,
    val htmlUrl: String,
    val followersUrl: String,
    val followingUrl: String,
    val gistsUrl: String,
    val starredUrl: String,
    val subscriptionsUrl: String,
    val organizationsUrl: String,
    val reposUrl: String,
    val eventsUrl: String,
    val receivedEventsUrl: String,
    val type: String,
    val siteAdmin: Boolean
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: GithubRepoOwner = other as GithubRepoOwner
        if (this.login != o.login)
            return false
        if (this.id != o.id)
            return false
        if (this.nodeId != o.nodeId)
            return false
        if (this.avatarUrl != o.avatarUrl)
            return false
        if (this.gravatarId != o.gravatarId)
            return false
        if (this.url != o.url)
            return false
        if (this.htmlUrl != o.htmlUrl)
            return false
        if (this.followersUrl != o.followersUrl)
            return false
        if (this.followingUrl != o.followingUrl)
            return false
        if (this.gistsUrl != o.gistsUrl)
            return false
        if (this.starredUrl != o.starredUrl)
            return false
        if (this.subscriptionsUrl != o.subscriptionsUrl)
            return false
        if (this.organizationsUrl != o.organizationsUrl)
            return false
        if (this.reposUrl != o.reposUrl)
            return false
        if (this.eventsUrl != o.eventsUrl)
            return false
        if (this.receivedEventsUrl != o.receivedEventsUrl)
            return false
        if (this.type != o.type)
            return false
        if (this.siteAdmin != o.siteAdmin)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + this.login.hashCode()
        result = prime * result + this.id.hashCode()
        result = prime * result + this.nodeId.hashCode()
        result = prime * result + this.avatarUrl.hashCode()
        result = prime * result + this.gravatarId.hashCode()
        result = prime * result + this.url.hashCode()
        result = prime * result + this.htmlUrl.hashCode()
        result = prime * result + this.followersUrl.hashCode()
        result = prime * result + this.followingUrl.hashCode()
        result = prime * result + this.gistsUrl.hashCode()
        result = prime * result + this.starredUrl.hashCode()
        result = prime * result + this.subscriptionsUrl.hashCode()
        result = prime * result + this.organizationsUrl.hashCode()
        result = prime * result + this.reposUrl.hashCode()
        result = prime * result + this.eventsUrl.hashCode()
        result = prime * result + this.receivedEventsUrl.hashCode()
        result = prime * result + this.type.hashCode()
        result = prime * result + this.siteAdmin.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("GithubRepoOwner (")

        sb.append(login)
        sb.append(", ").append(id)
        sb.append(", ").append(nodeId)
        sb.append(", ").append(avatarUrl)
        sb.append(", ").append(gravatarId)
        sb.append(", ").append(url)
        sb.append(", ").append(htmlUrl)
        sb.append(", ").append(followersUrl)
        sb.append(", ").append(followingUrl)
        sb.append(", ").append(gistsUrl)
        sb.append(", ").append(starredUrl)
        sb.append(", ").append(subscriptionsUrl)
        sb.append(", ").append(organizationsUrl)
        sb.append(", ").append(reposUrl)
        sb.append(", ").append(eventsUrl)
        sb.append(", ").append(receivedEventsUrl)
        sb.append(", ").append(type)
        sb.append(", ").append(siteAdmin)

        sb.append(")")
        return sb.toString()
    }
}
