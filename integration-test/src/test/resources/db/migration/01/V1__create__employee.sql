------ -- -- -- ------
------ EMPLOYEE ------
------ -- -- -- ------


-- -- -- -- -- create table if not exists
create table if not exists employee
(
    email       varchar(255) primary key,
    first_name  varchar(255),
    last_name   varchar(255)
);

alter table if exists employee
    owner to "device-manager";


-- -- -- -- -- -- -- --
--  DEFAULT EMPLOYEE --
-- -- -- -- -- -- -- --


INSERT INTO employee(email, first_name, last_name)
VALUES ('device.manager@exceet.de', 'Manager', 'Device')
ON CONFLICT DO NOTHING;
