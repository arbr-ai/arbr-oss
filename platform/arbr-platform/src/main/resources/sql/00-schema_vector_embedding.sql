create extension if not exists vector;

create table if not exists vector_embedding
(
    -- Vector ID for the embedding
    vector_id         varchar(511)   not null unique,

    -- A namespace carried over from Pinecone.
    namespace varchar(511) not null,

    -- A version ID of the content of the associated resource, such as the last commit hash where a file was edited.
    version_id varchar(511) not null,

    -- An id for the schema defining the category of content.
    schema_id varchar(511) not null,

    -- The actual string content that was embedded.
    embedding_content varchar(65535) not null,

    -- The embedding vector.
    embedding vector(1536) not null,

    primary key (vector_id)
);
