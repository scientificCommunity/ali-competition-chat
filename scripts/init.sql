drop table if exists g_user;
create table g_user
(
    id         serial8 not null primary key,
    username   varchar not null unique,
    password   varchar not null,
    firstName varchar not null,
    lastName  varchar not null,
    email      varchar not null,
    phone      varchar not null
);
comment on table g_user is '用户';
--create index g_user_username_password_idx on g_user(username,password);

drop table if exists g_room;
create table g_room
(
    id         serial8 not null primary key,
    name       varchar not null unique
--    obfuscated_id varchar not null,
--    created_by bigint  not null
);
comment on table g_room is '聊天室';
--comment on column g_room.created_by is '聊天室创建者id';
--comment on column g_room.obfuscated_roomId is '混淆后的roomId';

drop table if exists g_message;
create table g_message
(
    id         varchar  not null unique,
    text       text     not null,
    room_id   bigint   not null,
    timestamp timestamp         default now()
--    state      smallint not null default 0
);
comment on table g_message is '消息表';
comment on column g_message.room_id is '房间id';
create index g_message_room_id_idx on g_message(room_id);
create index on g_message(timestamp);