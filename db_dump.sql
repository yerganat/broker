create table excel
(
    id               bigint       not null
        primary key,
    bot_action_time  int          null,
    bot_file_id      text null,
    bot_send_file_id text null,
    bot_user_id      bigint       null,
    bytes            bigint       not null,
    description      text null,
    hash             text not null,
    name             text null,
    processed        bit          null,
    timestamp        datetime(6)  not null,
    user             text not null
);

create table exchange
(
    id      bigint not null
        primary key,
    date    date   not null,
    exclude bit    null,
    rate    double not null,
    constraint UK_mdabhj5ol5wy9d2ds3d271lc5
        unique (date)
);

create table hibernate_sequence
(
    next_val bigint null
);

create table payment
(
    id           bigint       not null
        primary key,
    amount       bigint       null,
    bot_user_id  bigint       null,
    is_payed     bit          null,
    paybox_tx_id bigint       null,
    tickers      text null,
    timestamp    datetime(6)  not null
);

create table setting
(
    id    bigint       not null
        primary key,
    name  text not null,
    value text null
);

create table user
(
    id          bigint       not null
        primary key,
    bot_user_id bigint       not null,
    description text null,
    timestamp   datetime(6)  not null,
    user        text null
);

