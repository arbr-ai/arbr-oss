create table user_project
(
    id                 bigserial      not null unique,
    user_id            bigint         not null references user_account (id),
    creation_timestamp bigint         not null,
    full_name          varchar(65535) not null, -- full repo name

    primary key (user_id, full_name)            -- todo: maybe this should be a constraint and id should be primary?
);
create index user_project_id_idx on user_project (id);

create table user_project_workflow
(
    id                             bigserial    not null unique,
    project_id                     bigint       not null references user_project (id),
    creation_timestamp             bigint       not null,
    workflow_type                  varchar(511) not null, -- the kind of workflow
    last_status                    int          not null, -- the most recently recorded status
    last_status_recorded_timestamp bigint       not null,
    plan_info                      jsonb        not null,
    commit_info                    jsonb        not null,
    requested_user_inputs          jsonb        not null,
    valued_user_inputs             jsonb        not null,
    build_artifacts                jsonb        not null, -- outputs of the build
    param_map                      jsonb,                 -- input parameter map
    idempotency_key                varchar(511)
);
create index user_project_workflow_project_id_idx on user_project_workflow (project_id);
create index user_project_workflow_id_idx on user_project_workflow (id);

-- Used to let a worker task claim a workflow
create table user_project_workflow_worker
(
    id                 bigserial not null primary key unique,
    creation_timestamp bigint    not null,
    workflow_id        bigint references user_project_workflow (id) unique,
    worker_uuid        varchar(127) -- UUID for the worker to verify ownership
);

-- Resources created as part of the workflow for output
create table user_project_workflow_resource
(
    id                 bigserial    not null unique,
    object_model_uuid  varchar(255) not null unique,
    creation_timestamp bigint       not null,
    updated_timestamp  bigint       not null,
    workflow_id        bigint       not null references user_project_workflow (id),
    resource_type      varchar(511) not null,
    parent_resource_id bigint references user_project_workflow_resource (id),
    resource_data      jsonb        not null, -- body of resource
    ordinal            int          not null, -- index in collection
    is_valid           boolean      not null, -- whether resource is still valid
    primary key (id)
);
create index user_project_workflow_resource_workflow_id_idx on user_project_workflow_resource (workflow_id);
create index user_project_workflow_resource_uuid_idx on user_project_workflow_resource (object_model_uuid);

-- Record relating hashes of states to offsets in their stream.
create table user_project_workflow_event_stream
(
    id              bigserial    not null unique,
    workflow_id     bigint       not null references user_project_workflow (id),
    topic_name      varchar(255) not null, -- topic name for the stream
    state_hash      varchar(255) not null,
    ordinal_offset  bigint       not null,
    event_timestamp bigint       not null, -- non-canonical timestamp of recording hash-offset relationship
    primary key (id)
);
create index user_project_workflow_event_stream_workflow_id_idx on user_project_workflow_event_stream (workflow_id);
create index user_project_workflow_event_stream_topic_name_idx on user_project_workflow_event_stream (topic_name);
create index user_project_workflow_event_stream_state_hash_idx on user_project_workflow_event_stream (state_hash);
create index user_project_workflow_event_stream_offset_idx on user_project_workflow_event_stream (ordinal_offset);
