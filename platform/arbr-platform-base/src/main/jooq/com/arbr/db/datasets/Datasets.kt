/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets


import com.arbr.db.DefaultCatalog
import com.arbr.db.datasets.tables.GithubCommit
import com.arbr.db.datasets.tables.GithubCommitFile
import com.arbr.db.datasets.tables.GithubCommitInnerCommit
import com.arbr.db.datasets.tables.GithubCommitRecord
import com.arbr.db.datasets.tables.GithubCommitStats
import com.arbr.db.datasets.tables.GithubFileContents
import com.arbr.db.datasets.tables.GithubFileParseRuleContext
import com.arbr.db.datasets.tables.GithubFileParseTerminalNode
import com.arbr.db.datasets.tables.GithubFileParseToken
import com.arbr.db.datasets.tables.GithubFileParseTree
import com.arbr.db.datasets.tables.GithubFileParseTreeNode
import com.arbr.db.datasets.tables.GithubFileReference
import com.arbr.db.datasets.tables.GithubPullRequest
import com.arbr.db.datasets.tables.GithubPullRequestRecord
import com.arbr.db.datasets.tables.GithubRepo
import com.arbr.db.datasets.tables.GithubRepoOwner

import kotlin.collections.List

import org.jooq.Catalog
import org.jooq.Table
import org.jooq.impl.SchemaImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Datasets : SchemaImpl("datasets", DefaultCatalog.DEFAULT_CATALOG) {
    public companion object {

        /**
         * The reference instance of <code>datasets</code>
         */
        val DATASETS: Datasets = Datasets()
    }

    /**
     * The table <code>datasets.github_commit</code>.
     */
    val GITHUB_COMMIT: GithubCommit get() = GithubCommit.GITHUB_COMMIT

    /**
     * The table <code>datasets.github_commit_file</code>.
     */
    val GITHUB_COMMIT_FILE: GithubCommitFile get() = GithubCommitFile.GITHUB_COMMIT_FILE

    /**
     * The table <code>datasets.github_commit_inner_commit</code>.
     */
    val GITHUB_COMMIT_INNER_COMMIT: GithubCommitInnerCommit get() = GithubCommitInnerCommit.GITHUB_COMMIT_INNER_COMMIT

    /**
     * The table <code>datasets.github_commit_record</code>.
     */
    val GITHUB_COMMIT_RECORD: GithubCommitRecord get() = GithubCommitRecord.GITHUB_COMMIT_RECORD

    /**
     * The table <code>datasets.github_commit_stats</code>.
     */
    val GITHUB_COMMIT_STATS: GithubCommitStats get() = GithubCommitStats.GITHUB_COMMIT_STATS

    /**
     * The table <code>datasets.github_file_contents</code>.
     */
    val GITHUB_FILE_CONTENTS: GithubFileContents get() = GithubFileContents.GITHUB_FILE_CONTENTS

    /**
     * The table <code>datasets.github_file_parse_rule_context</code>.
     */
    val GITHUB_FILE_PARSE_RULE_CONTEXT: GithubFileParseRuleContext get() = GithubFileParseRuleContext.GITHUB_FILE_PARSE_RULE_CONTEXT

    /**
     * The table <code>datasets.github_file_parse_terminal_node</code>.
     */
    val GITHUB_FILE_PARSE_TERMINAL_NODE: GithubFileParseTerminalNode get() = GithubFileParseTerminalNode.GITHUB_FILE_PARSE_TERMINAL_NODE

    /**
     * The table <code>datasets.github_file_parse_token</code>.
     */
    val GITHUB_FILE_PARSE_TOKEN: GithubFileParseToken get() = GithubFileParseToken.GITHUB_FILE_PARSE_TOKEN

    /**
     * The table <code>datasets.github_file_parse_tree</code>.
     */
    val GITHUB_FILE_PARSE_TREE: GithubFileParseTree get() = GithubFileParseTree.GITHUB_FILE_PARSE_TREE

    /**
     * The table <code>datasets.github_file_parse_tree_node</code>.
     */
    val GITHUB_FILE_PARSE_TREE_NODE: GithubFileParseTreeNode get() = GithubFileParseTreeNode.GITHUB_FILE_PARSE_TREE_NODE

    /**
     * The table <code>datasets.github_file_reference</code>.
     */
    val GITHUB_FILE_REFERENCE: GithubFileReference get() = GithubFileReference.GITHUB_FILE_REFERENCE

    /**
     * The table <code>datasets.github_pull_request</code>.
     */
    val GITHUB_PULL_REQUEST: GithubPullRequest get() = GithubPullRequest.GITHUB_PULL_REQUEST

    /**
     * The table <code>datasets.github_pull_request_record</code>.
     */
    val GITHUB_PULL_REQUEST_RECORD: GithubPullRequestRecord get() = GithubPullRequestRecord.GITHUB_PULL_REQUEST_RECORD

    /**
     * The table <code>datasets.github_repo</code>.
     */
    val GITHUB_REPO: GithubRepo get() = GithubRepo.GITHUB_REPO

    /**
     * The table <code>datasets.github_repo_owner</code>.
     */
    val GITHUB_REPO_OWNER: GithubRepoOwner get() = GithubRepoOwner.GITHUB_REPO_OWNER

    override fun getCatalog(): Catalog = DefaultCatalog.DEFAULT_CATALOG

    override fun getTables(): List<Table<*>> = listOf(
        GithubCommit.GITHUB_COMMIT,
        GithubCommitFile.GITHUB_COMMIT_FILE,
        GithubCommitInnerCommit.GITHUB_COMMIT_INNER_COMMIT,
        GithubCommitRecord.GITHUB_COMMIT_RECORD,
        GithubCommitStats.GITHUB_COMMIT_STATS,
        GithubFileContents.GITHUB_FILE_CONTENTS,
        GithubFileParseRuleContext.GITHUB_FILE_PARSE_RULE_CONTEXT,
        GithubFileParseTerminalNode.GITHUB_FILE_PARSE_TERMINAL_NODE,
        GithubFileParseToken.GITHUB_FILE_PARSE_TOKEN,
        GithubFileParseTree.GITHUB_FILE_PARSE_TREE,
        GithubFileParseTreeNode.GITHUB_FILE_PARSE_TREE_NODE,
        GithubFileReference.GITHUB_FILE_REFERENCE,
        GithubPullRequest.GITHUB_PULL_REQUEST,
        GithubPullRequestRecord.GITHUB_PULL_REQUEST_RECORD,
        GithubRepo.GITHUB_REPO,
        GithubRepoOwner.GITHUB_REPO_OWNER
    )
}
