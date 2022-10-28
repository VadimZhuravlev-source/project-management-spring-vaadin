
CREATE TABLE Users
(
    id        	SERIAL      NOT NULL,
    phone_number varchar(11),
    first_name   varchar(30),
    last_name    varchar(30),
    role        varchar(30),
    address     varchar(100),
    is_active    boolean,
    password    bytea,
    CONSTRAINT "pk_Users" PRIMARY KEY (id),
    CONSTRAINT "uc_Users_phone_number" UNIQUE (phone_number)
);

INSERT INTO Users(
    id, phone_number, first_name, last_name, role, address, is_active, password)
    VALUES (0, '000', 'ADMIN', 'ADMIN', 'ADMIN', 'ADMIN', 'true', 'ADMIN');