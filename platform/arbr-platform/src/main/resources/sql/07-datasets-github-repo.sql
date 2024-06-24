-- User
create table if not exists "datasets"."github_repo_owner"
(
    login               text    not null primary key,
    id                  integer not null,
    node_id             text    not null,
    avatar_url          text    not null,
    gravatar_id         text    not null,
    url                 text    not null,
    html_url            text    not null,
    followers_url       text    not null,
    following_url       text    not null,
    gists_url           text    not null,
    starred_url         text    not null,
    subscriptions_url   text    not null,
    organizations_url   text    not null,
    repos_url           text    not null,
    events_url          text    not null,
    received_events_url text    not null,
    type                text    not null,
    site_admin          boolean not null
);

-- User.Repo
create table if not exists "datasets"."github_repo"
(
    id                          bigint  not null primary key,
    node_id                     text    not null,
    name                        text    not null,
    full_name                   text    not null,
    private                     boolean not null,
    owner_login                 text    not null,
    html_url                    text    not null,
    description                 text,
    fork                        boolean not null,
    url                         text    not null,
    forks_url                   text    not null,
    keys_url                    text    not null,
    collaborators_url           text    not null,
    teams_url                   text    not null,
    hooks_url                   text    not null,
    issue_events_url            text    not null,
    events_url                  text    not null,
    assignees_url               text    not null,
    branches_url                text    not null,
    tags_url                    text    not null,
    blobs_url                   text    not null,
    git_tags_url                text    not null,
    git_refs_url                text    not null,
    trees_url                   text    not null,
    statuses_url                text    not null,
    languages_url               text    not null,
    stargazers_url              text    not null,
    contributors_url            text    not null,
    subscribers_url             text    not null,
    subscription_url            text    not null,
    commits_url                 text    not null,
    git_commits_url             text    not null,
    comments_url                text    not null,
    issue_comment_url           text    not null,
    contents_url                text    not null,
    compare_url                 text    not null,
    merges_url                  text    not null,
    archive_url                 text    not null,
    downloads_url               text    not null,
    issues_url                  text    not null,
    pulls_url                   text    not null,
    milestones_url              text    not null,
    notifications_url           text    not null,
    labels_url                  text    not null,
    releases_url                text    not null,
    deployments_url             text    not null,
    created_at                  text    not null,
    updated_at                  text    not null,
    pushed_at                   text    not null,
    git_url                     text    not null,
    ssh_url                     text    not null,
    clone_url                   text    not null,
    svn_url                     text    not null,
    homepage                    text,
    size                        integer not null,
    stargazers_count            integer not null,
    watchers_count              integer not null,
    language                    text,
    has_issues                  boolean not null,
    has_projects                boolean not null,
    has_downloads               boolean not null,
    has_wiki                    boolean not null,
    has_pages                   boolean not null,
    has_discussions             boolean not null,
    forks_count                 integer not null,
    mirror_url                  text,
    archived                    boolean not null,
    disabled                    boolean not null,
    open_issues_count           integer not null,
    license                     text,
    allow_forking               boolean not null,
    is_template                 boolean not null,
    web_commit_signoff_required boolean not null,
    visibility                  text    not null,
    forks                       integer not null,
    open_issues                 integer not null,
    watchers                    integer not null,
    default_branch              text    not null,
    temp_clone_token            text,
    network_count               integer not null,
    subscribers_count           integer not null,
    foreign key (owner_login) references "datasets"."github_repo_owner" (login)
);

-- User.Repo.PullRequest
create table if not exists "datasets"."github_pull_request"
(
    id               bigint not null primary key,
    repo_id          bigint not null,
    title            text   not null,
    body             text,
    diff_url         text   not null,
    html_url         text   not null,
    patch_url        text   not null,
    merge_commit_sha varchar(40),
    commits_url      text   not null,
    foreign key (repo_id) references "datasets"."github_repo"(id)
);

-- User.Repo.PullRequest.Commit
create table if not exists "datasets"."github_commit"
(
    sha             varchar(40) not null primary key,
    pull_request_id bigint      not null,
    foreign key (pull_request_id) references "datasets"."github_pull_request" (id)
);
create index github_commit_sha_idx on "datasets"."github_commit" (sha);

-- User.Repo.PullRequest.Commit.File
create table if not exists "datasets"."github_commit_file"
(
    sha               varchar(40),
    filename          text        not null,
    status            text        not null,
    additions         bigint      not null,
    deletions         bigint      not null,
    changes           bigint      not null,
    blob_url          text,
    raw_url           text,
    contents_url      text        not null,
    patch             text,
    previous_filename text,
    commit_info_sha   varchar(40) not null,
    foreign key (commit_info_sha) references "datasets"."github_commit" (sha)
);

-- User.Repo.PullRequest.Commit.Stats
create table if not exists "datasets"."github_commit_stats"
(
    total           bigint      not null,
    additions       bigint      not null,
    deletions       bigint      not null,
    commit_info_sha varchar(40) not null,
    foreign key (commit_info_sha) references "datasets"."github_commit" (sha)
);

-- User.Repo.PullRequest.Commit.Commit
create table if not exists "datasets"."github_commit_inner_commit"
(
    message         text        not null,
    commit_info_sha varchar(40) not null,
    foreign key (commit_info_sha) references "datasets"."github_commit" (sha)
);
