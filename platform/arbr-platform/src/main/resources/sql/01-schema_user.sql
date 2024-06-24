create table user_account
(
    id                 bigserial      not null unique,
    creation_timestamp bigint         not null,
    username           varchar(63) unique,
    email              varchar(511) unique, -- used as login handle for first-party accounts; otherwise use `user_email`
    password_key       varchar(65535) not null,
    roles_bitmask      int            not null,
    avatar_url         varchar(511),
    primary key (id)
);

create table user_account_link_github
(
    creation_timestamp bigint not null,
    github_id          varchar(65535),
    github_key         varchar(65535),
    github_app_name    varchar(511),
    user_id            bigint not null references user_account (id)
);

-- create table user_account_link_figma
-- (
--     creation_timestamp bigint not null,
--     figma_id           varchar(65535),
--     figma_key          varchar(65535),
--     user_id            bigint not null references user_account (id)
-- );

create table alpha_code
(
    code varchar(255) not null,
    primary key (code)
);

create table user_account_link_alpha_code
(
    user_id bigint       not null unique references user_account (id),
    code    varchar(255) not null unique references alpha_code (code)
);

-- User may have multiple emails
create table user_email
(
    user_id            bigint       not null references user_account (id),

    creation_timestamp bigint       not null,
    updated_timestamp  bigint       not null,
    email_address      varchar(511) not null,
    source             varchar(31)  not null, -- first_party, github, ...

    -- GitHub email specific fields
    gh_verified        boolean,
    gh_primary         boolean,

    unique (user_id, email_address)
);
create index if not exists user_email_user_id on user_email (user_id);
create index if not exists user_email_email_address on user_email (email_address);

create table user_event
(
    user_id          bigint       not null references user_account (id),

    event_timestamp  bigint       not null,
    event_name       varchar(511) not null,
    event_properties jsonb
);
create index if not exists user_event_user_id on user_event (user_id);

-- create table user_subscription
-- (
--     subscription_id          bigserial    not null unique,
--     user_id                  bigint       not null references user_account (id),
--     creation_timestamp       bigint       not null,
--     source                   varchar(255) not null,
--     provider_subscription_id varchar(255) not null,
--     provider_user_id         varchar(255) not null,
--     provider_plan_id         varchar(255) not null,
--     status                   varchar(63)  not null,
--     is_renewing              boolean      not null,
--     period_end_ms            bigint       not null,
--     primary key (subscription_id)
-- );
-- create index if not exists user_subscription_user_id on user_subscription (user_id);
--
-- create table user_customer_info
-- (
--     user_id            bigint       not null references user_account (id),
--     creation_timestamp bigint       not null,
--     source             varchar(255) not null,
--     provider_user_id   varchar(255) not null,
--     email              varchar(255),
--     name               varchar(255),
--     primary key (user_id)
-- );
--
-- create index if not exists user_customer_info_user_id on user_customer_info (user_id);
--
-- create table user_payment_method_info
-- (
--     user_id           bigint       not null references user_account (id),
--     source            varchar(255) not null,
--     payment_method_id varchar(255) not null,
--     primary key (user_id, payment_method_id)
-- );
--
-- create index if not exists user_payment_method_info_user_id on user_payment_method_info (user_id);
--
-- -- An event on a subscription, such as trial start, conversion, renewal, cancellation, etc.
-- create table user_subscription_event
-- (
--     id              bigserial    not null,
--     subscription_id bigint       not null references user_subscription (subscription_id),
--     user_id         bigint       not null references user_account (id),
--     event_kind      varchar(255) not null,
--     event_hash      varchar(255) not null,
--     event_timestamp bigint       not null,
--     primary key (id)
-- );
-- create index if not exists user_subscription_event_subscription_id on user_subscription_event (subscription_id);
-- create index if not exists user_subscription_event_user_id on user_subscription_event (user_id);
--
-- -- A reward granted to a user via a subscription event.
-- create table user_subscription_reward
-- (
--     id                       bigserial    not null,
--     subscription_id          bigint       not null references user_subscription (subscription_id),
--     user_id                  bigint       not null references user_account (id),
--     reward_kind              varchar(255) not null,
--     reward_ordinal           bigint       not null,
--     reward_hash              varchar(255) not null unique,
--     reward_granted_timestamp bigint       not null,
--     primary key (id),
--     constraint uq_user_subscription_reward_once unique (subscription_id, user_id, reward_kind, reward_ordinal)
-- );
-- create index if not exists user_subscription_reward_subscription_id on user_subscription_reward (subscription_id);
-- create index if not exists user_subscription_reward_user_id on user_subscription_reward (user_id);
--
-- -- A baseline for a user's credit ledger computation, effectively defaulting to 0 when missing.
-- create table user_credit_ledger_state
-- (
--     id                   bigserial    not null,
--     user_id              bigint       not null references user_account (id),
--     event_timestamp      bigint       not null, -- Effectively the timestamp of the last event making up the ledger.
--     ledger_state_ordinal bigint       not null,
--     ledger_state_hash    varchar(255) not null unique,
--     credit_kind          varchar(255) not null,
--     quantity             bigint       not null,
--     primary key (id),
--     constraint uq_user_credit_ledger_state_once unique (user_id, credit_kind, ledger_state_ordinal)
-- );
-- create index if not exists user_credit_ledger_state_user_id on user_credit_ledger_state (user_id);
-- create index if not exists user_credit_ledger_state_ledger_state_hash on user_credit_ledger_state (ledger_state_hash);
-- create index if not exists user_credit_ledger_state_ledger_state_ordinal on user_credit_ledger_state (credit_kind, ledger_state_ordinal);
--
-- -- A change in a user's credits, as a ledger event.
-- create table user_credit_ledger_event
-- (
--     id                   bigserial    not null,
--     user_id              bigint       not null references user_account (id),
--     event_timestamp      bigint       not null,
--     ledger_event_ordinal bigint       not null,
--     ledger_event_hash    varchar(255) not null unique,
--     credit_kind          varchar(255) not null,
--     operation            varchar(255) not null, -- one of add, set
--     quantity             bigint       not null,
--     primary key (id),
--     constraint uq_user_credit_ledger_event_once unique (user_id, credit_kind, ledger_event_ordinal)
-- );
-- create index if not exists user_credit_ledger_event_user_id on user_credit_ledger_event (user_id);
-- create index if not exists user_credit_ledger_event_ledger_state_hash on user_credit_ledger_event (ledger_event_hash);
-- create index if not exists user_credit_ledger_event_ledger_state_ordinal on user_credit_ledger_event (credit_kind, ledger_event_ordinal);
