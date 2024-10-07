create table historical_split
(
    symbol      varchar(20)      not null,
    date        DATE             not null,
    numerator   double precision not null,
    denominator double precision not null,
    constraint historical_split_pk
        primary key (symbol, date)
);

create table historical_quote
(
    symbol      varchar(20)      not null,
    date        DATE             not null,
    open double precision not null,
    low double precision not null,
    high double precision not null,
    close double precision not null,
    adjClose double precision not null,
    volume double precision not null,
    constraint historical_split_pk
        primary key (symbol, date)
);

