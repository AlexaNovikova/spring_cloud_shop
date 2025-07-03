insert into auth.role values('bdb15da0-8c8a-4aaf-b353-99481a55692e', 'CLIENT');
insert into auth.role values('34ff17b8-ba19-4a45-878c-2ab70571ecfe', 'EMPLOYEE');
insert into auth.role values('25891fc0-9183-472b-be52-4a0972ed8649', 'ADMIN');

--Тестовый администратор
INSERT INTO auth."user"
(id, last_name, first_name, second_name, email, created_at)
VALUES('a918c731-36f9-4b61-873e-4b7e5ef49c0f', 'Админов', 'Админ', 'Админович', 'admin@romashka.ru', now());

INSERT INTO auth.identifier
(id, user_id, login, "blocked", secret)
VALUES('ce9adbbd-9b1d-459a-a45e-1114c6aa3e8d', 'a918c731-36f9-4b61-873e-4b7e5ef49c0f', 'admin@romashka.ru', false, '$2y$10$r0UWQDeQmPAaMYrv.TWu7.hZbkTvweSi5IlJCZ0kuIUJNAVYvYNMS');

INSERT INTO auth.user_role
(user_id, role_id)
VALUES('a918c731-36f9-4b61-873e-4b7e5ef49c0f', '25891fc0-9183-472b-be52-4a0972ed8649');

INSERT INTO auth."user"
(id, last_name, first_name, second_name, email, created_at)
VALUES('aa3f35c5-3621-4957-9c85-eb21756f2dda', 'Петров', 'Игорь', 'Сергеевич', 'petrov@shop.ru', now());

INSERT INTO auth.identifier
(id, user_id, login, "blocked", secret)
VALUES('548d06fd-6fcc-4ebb-b76a-56c651789d13', 'aa3f35c5-3621-4957-9c85-eb21756f2dda', 'petrov@shop.ru', false, '$2y$10$r0UWQDeQmPAaMYrv.TWu7.hZbkTvweSi5IlJCZ0kuIUJNAVYvYNMS');

INSERT INTO auth.user_role
(user_id, role_id)
VALUES('aa3f35c5-3621-4957-9c85-eb21756f2dda', '34ff17b8-ba19-4a45-878c-2ab70571ecfe');