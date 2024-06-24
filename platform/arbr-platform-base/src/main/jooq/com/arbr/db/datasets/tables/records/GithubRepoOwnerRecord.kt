/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables.records


import com.arbr.db.datasets.tables.GithubRepoOwner

import org.jooq.Field
import org.jooq.Record1
import org.jooq.Record18
import org.jooq.Row18
import org.jooq.impl.UpdatableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class GithubRepoOwnerRecord private constructor() : UpdatableRecordImpl<GithubRepoOwnerRecord>(GithubRepoOwner.GITHUB_REPO_OWNER), Record18<String?, Int?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, Boolean?> {

    open var login: String
        set(value): Unit = set(0, value)
        get(): String = get(0) as String

    open var id: Int
        set(value): Unit = set(1, value)
        get(): Int = get(1) as Int

    open var nodeId: String
        set(value): Unit = set(2, value)
        get(): String = get(2) as String

    open var avatarUrl: String
        set(value): Unit = set(3, value)
        get(): String = get(3) as String

    open var gravatarId: String
        set(value): Unit = set(4, value)
        get(): String = get(4) as String

    open var url: String
        set(value): Unit = set(5, value)
        get(): String = get(5) as String

    open var htmlUrl: String
        set(value): Unit = set(6, value)
        get(): String = get(6) as String

    open var followersUrl: String
        set(value): Unit = set(7, value)
        get(): String = get(7) as String

    open var followingUrl: String
        set(value): Unit = set(8, value)
        get(): String = get(8) as String

    open var gistsUrl: String
        set(value): Unit = set(9, value)
        get(): String = get(9) as String

    open var starredUrl: String
        set(value): Unit = set(10, value)
        get(): String = get(10) as String

    open var subscriptionsUrl: String
        set(value): Unit = set(11, value)
        get(): String = get(11) as String

    open var organizationsUrl: String
        set(value): Unit = set(12, value)
        get(): String = get(12) as String

    open var reposUrl: String
        set(value): Unit = set(13, value)
        get(): String = get(13) as String

    open var eventsUrl: String
        set(value): Unit = set(14, value)
        get(): String = get(14) as String

    open var receivedEventsUrl: String
        set(value): Unit = set(15, value)
        get(): String = get(15) as String

    open var type: String
        set(value): Unit = set(16, value)
        get(): String = get(16) as String

    open var siteAdmin: Boolean
        set(value): Unit = set(17, value)
        get(): Boolean = get(17) as Boolean

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    override fun key(): Record1<String?> = super.key() as Record1<String?>

    // -------------------------------------------------------------------------
    // Record18 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row18<String?, Int?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, Boolean?> = super.fieldsRow() as Row18<String?, Int?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, Boolean?>
    override fun valuesRow(): Row18<String?, Int?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, Boolean?> = super.valuesRow() as Row18<String?, Int?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, Boolean?>
    override fun field1(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.LOGIN
    override fun field2(): Field<Int?> = GithubRepoOwner.GITHUB_REPO_OWNER.ID
    override fun field3(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.NODE_ID
    override fun field4(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.AVATAR_URL
    override fun field5(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.GRAVATAR_ID
    override fun field6(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.URL
    override fun field7(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.HTML_URL
    override fun field8(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.FOLLOWERS_URL
    override fun field9(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.FOLLOWING_URL
    override fun field10(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.GISTS_URL
    override fun field11(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.STARRED_URL
    override fun field12(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.SUBSCRIPTIONS_URL
    override fun field13(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.ORGANIZATIONS_URL
    override fun field14(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.REPOS_URL
    override fun field15(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.EVENTS_URL
    override fun field16(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.RECEIVED_EVENTS_URL
    override fun field17(): Field<String?> = GithubRepoOwner.GITHUB_REPO_OWNER.TYPE
    override fun field18(): Field<Boolean?> = GithubRepoOwner.GITHUB_REPO_OWNER.SITE_ADMIN
    override fun component1(): String = login
    override fun component2(): Int = id
    override fun component3(): String = nodeId
    override fun component4(): String = avatarUrl
    override fun component5(): String = gravatarId
    override fun component6(): String = url
    override fun component7(): String = htmlUrl
    override fun component8(): String = followersUrl
    override fun component9(): String = followingUrl
    override fun component10(): String = gistsUrl
    override fun component11(): String = starredUrl
    override fun component12(): String = subscriptionsUrl
    override fun component13(): String = organizationsUrl
    override fun component14(): String = reposUrl
    override fun component15(): String = eventsUrl
    override fun component16(): String = receivedEventsUrl
    override fun component17(): String = type
    override fun component18(): Boolean = siteAdmin
    override fun value1(): String = login
    override fun value2(): Int = id
    override fun value3(): String = nodeId
    override fun value4(): String = avatarUrl
    override fun value5(): String = gravatarId
    override fun value6(): String = url
    override fun value7(): String = htmlUrl
    override fun value8(): String = followersUrl
    override fun value9(): String = followingUrl
    override fun value10(): String = gistsUrl
    override fun value11(): String = starredUrl
    override fun value12(): String = subscriptionsUrl
    override fun value13(): String = organizationsUrl
    override fun value14(): String = reposUrl
    override fun value15(): String = eventsUrl
    override fun value16(): String = receivedEventsUrl
    override fun value17(): String = type
    override fun value18(): Boolean = siteAdmin

    override fun value1(value: String?): GithubRepoOwnerRecord {
        set(0, value)
        return this
    }

    override fun value2(value: Int?): GithubRepoOwnerRecord {
        set(1, value)
        return this
    }

    override fun value3(value: String?): GithubRepoOwnerRecord {
        set(2, value)
        return this
    }

    override fun value4(value: String?): GithubRepoOwnerRecord {
        set(3, value)
        return this
    }

    override fun value5(value: String?): GithubRepoOwnerRecord {
        set(4, value)
        return this
    }

    override fun value6(value: String?): GithubRepoOwnerRecord {
        set(5, value)
        return this
    }

    override fun value7(value: String?): GithubRepoOwnerRecord {
        set(6, value)
        return this
    }

    override fun value8(value: String?): GithubRepoOwnerRecord {
        set(7, value)
        return this
    }

    override fun value9(value: String?): GithubRepoOwnerRecord {
        set(8, value)
        return this
    }

    override fun value10(value: String?): GithubRepoOwnerRecord {
        set(9, value)
        return this
    }

    override fun value11(value: String?): GithubRepoOwnerRecord {
        set(10, value)
        return this
    }

    override fun value12(value: String?): GithubRepoOwnerRecord {
        set(11, value)
        return this
    }

    override fun value13(value: String?): GithubRepoOwnerRecord {
        set(12, value)
        return this
    }

    override fun value14(value: String?): GithubRepoOwnerRecord {
        set(13, value)
        return this
    }

    override fun value15(value: String?): GithubRepoOwnerRecord {
        set(14, value)
        return this
    }

    override fun value16(value: String?): GithubRepoOwnerRecord {
        set(15, value)
        return this
    }

    override fun value17(value: String?): GithubRepoOwnerRecord {
        set(16, value)
        return this
    }

    override fun value18(value: Boolean?): GithubRepoOwnerRecord {
        set(17, value)
        return this
    }

    override fun values(value1: String?, value2: Int?, value3: String?, value4: String?, value5: String?, value6: String?, value7: String?, value8: String?, value9: String?, value10: String?, value11: String?, value12: String?, value13: String?, value14: String?, value15: String?, value16: String?, value17: String?, value18: Boolean?): GithubRepoOwnerRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        this.value5(value5)
        this.value6(value6)
        this.value7(value7)
        this.value8(value8)
        this.value9(value9)
        this.value10(value10)
        this.value11(value11)
        this.value12(value12)
        this.value13(value13)
        this.value14(value14)
        this.value15(value15)
        this.value16(value16)
        this.value17(value17)
        this.value18(value18)
        return this
    }

    /**
     * Create a detached, initialised GithubRepoOwnerRecord
     */
    constructor(login: String, id: Int, nodeId: String, avatarUrl: String, gravatarId: String, url: String, htmlUrl: String, followersUrl: String, followingUrl: String, gistsUrl: String, starredUrl: String, subscriptionsUrl: String, organizationsUrl: String, reposUrl: String, eventsUrl: String, receivedEventsUrl: String, type: String, siteAdmin: Boolean): this() {
        this.login = login
        this.id = id
        this.nodeId = nodeId
        this.avatarUrl = avatarUrl
        this.gravatarId = gravatarId
        this.url = url
        this.htmlUrl = htmlUrl
        this.followersUrl = followersUrl
        this.followingUrl = followingUrl
        this.gistsUrl = gistsUrl
        this.starredUrl = starredUrl
        this.subscriptionsUrl = subscriptionsUrl
        this.organizationsUrl = organizationsUrl
        this.reposUrl = reposUrl
        this.eventsUrl = eventsUrl
        this.receivedEventsUrl = receivedEventsUrl
        this.type = type
        this.siteAdmin = siteAdmin
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised GithubRepoOwnerRecord
     */
    constructor(value: com.arbr.db.datasets.tables.pojos.GithubRepoOwner?): this() {
        if (value != null) {
            this.login = value.login
            this.id = value.id
            this.nodeId = value.nodeId
            this.avatarUrl = value.avatarUrl
            this.gravatarId = value.gravatarId
            this.url = value.url
            this.htmlUrl = value.htmlUrl
            this.followersUrl = value.followersUrl
            this.followingUrl = value.followingUrl
            this.gistsUrl = value.gistsUrl
            this.starredUrl = value.starredUrl
            this.subscriptionsUrl = value.subscriptionsUrl
            this.organizationsUrl = value.organizationsUrl
            this.reposUrl = value.reposUrl
            this.eventsUrl = value.eventsUrl
            this.receivedEventsUrl = value.receivedEventsUrl
            this.type = value.type
            this.siteAdmin = value.siteAdmin
            resetChangedOnNotNull()
        }
    }
}