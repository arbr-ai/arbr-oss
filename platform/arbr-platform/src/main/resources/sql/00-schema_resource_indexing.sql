create table if not exists indexed_resource
(
    id                 bigserial    not null,
    creation_timestamp bigint       not null,

    -- Identifier for the resource schema.
    schema_id          varchar(511) not null,

    -- JSON Binary of the resource object
    resource_object    jsonb        not null,

    -- Optional chat-message-list formatted variant of the resource.
    chat_messages      jsonb,
    primary key (id)
);

create table if not exists embedded_content
(
    id                 bigserial      not null,
    creation_timestamp bigint         not null,

    -- 1-many relation between indexed resources and embedded content
    resource_id        bigint         not null references indexed_resource (id),

    -- Vector ID for the embedding
    vector_id          varchar(511)   not null,

    -- Identifier for the resource schema.
    schema_id          varchar(511)   not null,

    -- A key within the resource type for the kind of embedding, such as "description" or "summary"
    kind varchar(511) not null,

    -- The actual string content that was embedded.
    embedding_content  varchar(65535) not null,

    -- Metadata associated with the content
    metadata           jsonb,

    primary key (id)
);

create table if not exists io_pair
(
    id                 bigserial not null,
    creation_timestamp bigint    not null,

    -- input resource
    input_resource_id  bigint    not null references indexed_resource (id),

    -- output resource
    output_resource_id bigint    not null references indexed_resource (id)
);

create index if not exists embedded_content_vector_id on embedded_content (vector_id);
create index if not exists io_pair_input_resource_id on io_pair (input_resource_id);

-- views

create view embedded_resource_pair as
(
select
    ir.id as input_resource_id,
    ir.creation_timestamp as input_creation_timestamp,
    ir.schema_id as input_schema_id,
    ir.resource_object as input_resource_object,
    ir.chat_messages as input_chat_messages,
    ec.vector_id,
    ec.kind as embedding_kind,
    ec.embedding_content,
    ec.metadata,
    ir2.id as output_resource_id,
    ir2.creation_timestamp as output_creation_timestamp,
    ir2.schema_id as output_schema_id,
    ir2.resource_object as output_resource_object,
    ir2.chat_messages as output_chat_messages
from io_pair
         join indexed_resource ir on ir.id = io_pair.input_resource_id
         join indexed_resource ir2 on ir2.id = io_pair.output_resource_id
         join embedded_content ec on ir.id = ec.resource_id
    );
comment on view embedded_resource_pair is 'Input-Output pairs of Embedded Resources';
