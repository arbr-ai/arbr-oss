create table if not exists application_completion
(
    cache_key           varchar(511) not null,
    creation_timestamp  bigint       not null,
    application_id      varchar(255) not null,
    example_vector_ids  jsonb        not null,
    input_vector_ids    jsonb        not null,
    input_resource      jsonb        not null,
    output_resource     jsonb        not null,
    prompt_messages     jsonb        not null,
    completion_messages jsonb, -- Nullable for migration - should treat as not null
    workflow_id         bigint references user_project_workflow (id),
    used_model          varchar(255),
    prompt_tokens       bigint,
    completion_tokens   bigint,
    total_tokens        bigint,
    primary key (cache_key)
);


-- drop function if exists trace;
--
-- create or replace function trace(completion_cache_key varchar(511))
--     returns table
--             (
--                 cache_key                varchar(511),
--                 application_id           varchar(511),
--                 resource_index           bigint,
--                 type_name                text,
--                 value                    text,
--                 output_value             text,
--                 generator_index          bigint,
--                 generator_application_id text,
--                 generator_cache_key      text
--             )
-- as
-- $$
-- begin
--     return query with iresource as (select ac.cache_key,
--                                            ac.application_id,
--                                            ac.output_resource #>> '{}' as output_resource,
--                                            iresource.ordinality,
--                                            iresource.value
--                                     from application_completion ac
--                                              cross join lateral jsonb_array_elements(input_resource) with ordinality as iresource
--                                     where ac.cache_key = completion_cache_key),
--
--                       roots as (select iresource.cache_key,
--                                        iresource.application_id,
--                                        iresource.ordinality             as resource_index,
--                                        iresource.value #>> '{typeName}' as type_name,
--                                        iresource.value #>> '{value}'    as value,
--                                        iresource.output_resource        as output_value,
--                                        0                                as generator_index,
--                                        null                             as application_id,
--                                        null                             as cache_key
--                                 from iresource
--                                 where jsonb_array_length(iresource.value #> '{generatorInfo, generators}') = 0),
--
--                       sources_single as (select iresource.cache_key,
--                                                 iresource.application_id,
--                                                 iresource.ordinality                        as resource_index,
--                                                 iresource.value #>> '{typeName}'            as type_name,
--                                                 iresource.value #>> '{value}'               as value,
--                                                 iresource.output_resource                   as output_value,
--                                                 generators.ordinality                       as generator_index,
--                                                 generators.value #>> '{applicationId}'      as application_id,
--                                                 generators.value #>> '{completionCacheKey}' as cache_key
--                                          from iresource
--                                                   cross join lateral jsonb_array_elements(iresource.value #> '{generatorInfo, generators}') with ordinality as generators
--                                          where
--                                              jsonb_array_length(iresource.value #> '{generatorInfo, generators}') = 1),
--
--                       sources_multi as (select iresource.cache_key,
--                                                iresource.application_id,
--                                                iresource.ordinality                                              as resource_index,
--                                                iresource.value #>> '{typeName}'                                  as type_name,
--                                                iresource.value #> '{value}' ->> (generators.ordinality::int - 1) as value,
--                                                iresource.output_resource                                         as output_value,
--                                                generators.ordinality                                             as generator_index,
--                                                generators.value #>> '{applicationId}'                            as application_id,
--                                                generators.value #>> '{completionCacheKey}'                       as cache_key
--                                         from iresource
--                                                  cross join lateral jsonb_array_elements(iresource.value #> '{generatorInfo, generators}') with ordinality as generators
--                                         where jsonb_array_length(iresource.value #> '{generatorInfo, generators}') > 1)
--
--                      (select *
--                       from roots)
--                  UNION ALL
--                  (select *
--                   from sources_single)
--                  UNION ALL
--                  (select *
--                   from sources_multi)
--                  order by resource_index, generator_index;
-- end;
-- $$ LANGUAGE plpgsql;
--
--
-- drop function if exists trace_rec(completion_cache_key varchar(511), depth int);
--
-- create or replace function trace_rec(completion_cache_key varchar(511), depth int)
--     returns table
--             (
--                 completion_depth         int,
--                 cache_key                varchar(511),
--                 application_id           varchar(511),
--                 resource_index           bigint,
--                 type_name                text,
--                 value                    text,
--                 output_value             text,
--                 generator_index          bigint,
--                 generator_application_id text,
--                 generator_cache_key      text
--             )
-- as
-- $$
-- begin
--     EXECUTE format('drop table if exists %I', 'trace_' || completion_cache_key);
--
--     EXECUTE format('CREATE TEMP TABLE IF NOT EXISTS %I AS (select * from trace(''%s''))',
--                    'trace_' || completion_cache_key,
--                    completion_cache_key);
--
--     return query execute format('select %s, * from %I', depth, 'trace_' || completion_cache_key);
--
--     if depth > 1 then
--         return query execute format(
--                 'select inner_trace.* from %I cross join lateral trace_rec(%I.generator_cache_key, %s) as inner_trace where %I.generator_cache_key is not null',
--                 'trace_' || completion_cache_key, 'trace_' || completion_cache_key, depth - 1,
--                 'trace_' || completion_cache_key);
--     end if;
-- end
-- $$ language plpgsql;
--
--
-- drop function if exists trace_rec_match(completion_cache_key varchar(511), matches varchar(65535), depth int);
--
-- create or replace function trace_rec_match(completion_cache_key varchar(511), matches varchar(65535), depth int)
--     returns table
--             (
--                 completion_depth         int,
--                 cache_key                varchar(511),
--                 application_id           varchar(511),
--                 resource_index           bigint,
--                 type_name                text,
--                 value                    text,
--                 output_value             text,
--                 generator_index          bigint,
--                 generator_application_id text,
--                 generator_cache_key      text
--             )
-- as
-- $$
-- declare
--     tmp_table_name varchar(1023);
-- begin
--     select ('trace_' || completion_cache_key || '_' || now()) into tmp_table_name;
--     EXECUTE format(
--             'CREATE TEMP TABLE IF NOT EXISTS %I AS (select * from trace(''%s'') where value ilike ''%s'' or output_value ilike ''%s'')',
--             tmp_table_name,
--             completion_cache_key, matches, matches);
--
--     return query execute format('select %s, * from %I', depth, tmp_table_name);
--
--     if depth > 1 then
--         return query execute format(
--                 'select inner_trace.* from %I cross join lateral trace_rec_match(%I.generator_cache_key, ''%s'', %s) as inner_trace where %I.generator_cache_key is not null',
--                 tmp_table_name,
--                 tmp_table_name,
--                 matches,
--                 depth - 1,
--                 tmp_table_name
--                              );
--     end if;
-- end
-- $$ language plpgsql;
