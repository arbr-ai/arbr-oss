create table "datasets"."github_pull_request_record"
(
    pull_request_id bigint not null,
    tags            jsonb,
    foreign key (pull_request_id) references "datasets"."github_pull_request" (id)
);
create index github_pull_request_record_id_idx on "datasets"."github_pull_request_record" (pull_request_id);

create table "datasets"."github_commit_record"
(
    commit_info_sha varchar(40) not null,
    pull_request_id bigint      not null,
    ordinal         int         not null,
    tags            jsonb,
    foreign key (commit_info_sha) references "datasets"."github_commit" (sha),
    foreign key (pull_request_id) references "datasets"."github_pull_request" (id)
);
create index github_commit_record_pull_request_id_idx on "datasets"."github_commit_record" (pull_request_id);
