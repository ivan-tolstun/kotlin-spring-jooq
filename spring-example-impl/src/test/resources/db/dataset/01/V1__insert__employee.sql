------ -- -- -- -- -- -- -- ------
------ REMOVE OLD EMPLOYEE  ------
------ -- -- -- -- -- -- -- ------


DELETE FROM employee WHERE employee.email in ('Ivan.Tol@exceet.de', 'Ilgar.Bos@exceet.de', 'Roman.Rem@exceet.de');


-- -- -- -- -- -- -- --
--  DEFAULT EMPLOYEE --
-- -- -- -- -- -- -- --


INSERT INTO employee(email, first_name, last_name)
VALUES ('Ivan.Tol@exceet.de', 'Ivan', 'Tol'),
       ('Ilgar.Bos@exceet.de', 'Ilgar', 'Bos'),
       ('Roman.Rem@exceet.de', 'Roman', 'Rem')
ON CONFLICT DO NOTHING;
