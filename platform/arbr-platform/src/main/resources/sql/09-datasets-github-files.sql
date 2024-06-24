create table if not exists "datasets"."github_file_contents"
(
    commit_info_sha varchar(40),
    filename        text not null,
    contents        text not null
);

create index github_file_contents_commit_sha_idx on "datasets"."github_file_contents" (commit_info_sha);
create index github_file_contents_filename_idx on "datasets"."github_file_contents" (filename);
create index github_file_contents_commit_sha_filename_idx on "datasets"."github_file_contents" (commit_info_sha, filename);


create table if not exists "datasets"."github_file_parse_tree"
(
    commit_info_sha varchar(40),
    filename        text  not null,
    parse_tree      jsonb not null
);

create index github_file_parse_tree_commit_sha_idx on "datasets"."github_file_parse_tree" (commit_info_sha);
create index github_file_parse_tree_filename_idx on "datasets"."github_file_parse_tree" (filename);
create index github_file_parse_tree_commit_sha_filename_idx on "datasets"."github_file_parse_tree" (commit_info_sha, filename);



create table if not exists "datasets"."github_file_reference"
(
    id              bigserial     not null,
    repo_full_name  varchar(255)  not null,
    commit_info_sha varchar(40)   not null,
    filename        text          not null,

    -- URI of content, e.g.:
    --   file://com.arbr.datasets/repo_data/org/repo/deadbeef.zip!/app/main.js
    --   s3://datasets_bucket/repo_data/org/repo/33ffacb.zip!/app/main.js
    uri             varchar(1023) not null,

    primary key (id)
);
create index github_file_reference_commit_sha_idx on "datasets"."github_file_reference" (commit_info_sha);
create index github_file_reference_filename_idx on "datasets"."github_file_reference" (filename);
create index github_file_reference_commit_sha_filename_idx on "datasets"."github_file_reference" (commit_info_sha, filename);

create table if not exists "datasets"."github_file_parse_token"
(
    id                    bigserial not null,
    type                  int       not null,
    text                  text      not null,
    line                  int       not null,
    char_position_in_line int       not null,
    channel               int       not null,
    token_index           int       not null,
    start_index           int       not null,
    stop_index            int       not null,
    locus                 int,

    primary key (id)
);

create table if not exists "datasets"."github_file_parse_terminal_node"
(
    id       bigserial not null,
    token_id bigint    not null references "datasets"."github_file_parse_token" (id),
    is_error boolean   not null,

    primary key (id)
);

create table if not exists "datasets"."github_file_parse_rule_context"
(
    id             bigserial not null,
    rule_index     int       not null,
    child_count    int       not null,
    start_token_id bigint references "datasets"."github_file_parse_token" (id),
    stop_token_id  bigint references "datasets"."github_file_parse_token" (id),

    primary key (id)
);

create table if not exists "datasets"."github_file_parse_tree_node"
(
    id                bigserial not null,
    file_id           bigint    not null references "datasets"."github_file_reference" (id),
    filename          text      not null,
    push_index        int       not null,
    pop_index         int,
    parent_push_index int,
    rule_context_id   bigint references "datasets"."github_file_parse_rule_context" (id),
    terminal_node_id  bigint references "datasets"."github_file_parse_terminal_node" (id),

    primary key (id)
);
