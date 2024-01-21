DROP TABLE IF EXISTS "user_roles";
CREATE TABLE user_roles(
	id SERIAL,
	version INT,
	user_id INT NOT NULL,
	role_id INT NOT NULL,
	CONSTRAINT user_roles_pkey PRIMARY KEY (id),
	CONSTRAINT fk_user_roles_users_id
		FOREIGN KEY(user_id)
		REFERENCES users(id)
		ON DELETE CASCADE
);

DROP TABLE IF EXISTS "user_projects";
CREATE TABLE user_projects(
	id SERIAL,
	user_id INT NOT NULL,
	project_id INT NOT NULL,
	CONSTRAINT user_projects_pkey PRIMARY KEY (user_id, project_id),
	CONSTRAINT fk_user_projects_users_id
		FOREIGN KEY(user_id)
		REFERENCES users(id)
		ON DELETE CASCADE,
	CONSTRAINT fk_user_projects_project_tasks_id
		FOREIGN KEY(project_id)
		REFERENCES project_tasks(id)
		ON DELETE CASCADE
);

ALTER TABLE users
DROP COLUMN IF EXISTS phone_number;
/*
ALTER TABLE users
RENAME COLUMN name TO first_name;
*/
ALTER TABLE users
RENAME COLUMN first_name TO name;
ALTER TABLE users
DROP COLUMN IF EXISTS last_name;
ALTER TABLE users
DROP COLUMN IF EXISTS role;
ALTER TABLE users
DROP COLUMN IF EXISTS address;
ALTER TABLE users
DROP CONSTRAINT IF EXISTS  "uc_Users_phone_number";

ALTER TABLE users
DROP CONSTRAINT IF EXISTS "unique_users_name";
ALTER TABLE users
ADD CONSTRAINT "unique_users_name" UNIQUE (name);

ALTER TABLE users
DROP COLUMN IF EXISTS is_predefined;
ALTER TABLE users
ADD COLUMN is_predefined BOOLEAN DEFAULT FALSE;

ALTER TABLE users
DROP COLUMN IF EXISTS root_project_id;
ALTER TABLE users
ADD COLUMN root_project_id INT;
ALTER TABLE users
DROP CONSTRAINT IF EXISTS  "fk_users_project_tasks_id";
ALTER TABLE users
ADD CONSTRAINT fk_users_project_tasks_id
		FOREIGN KEY(root_project_id)
		REFERENCES project_tasks(id)
		ON DELETE SET NULL;

ALTER TABLE users
DROP COLUMN IF EXISTS access_type_id;
ALTER TABLE users
ADD COLUMN access_type_id INT NOT NULL DEFAULT 0;
ALTER TABLE users
DROP COLUMN IF EXISTS version;
ALTER TABLE users
ADD COLUMN version INT;

DELETE FROM users;
INSERT INTO Users(
    id, version, name, is_active, is_predefined, password, access_type_id)
    VALUES (1, 1, 'Admin', 'TRUE', 'TRUE', '1', 1);
	
INSERT INTO user_roles(
    id, version, user_id, role_id)
    VALUES (1, 1, 1, 0),
			(2, 1, 1, 1),
			(3, 1, 1, 2);
SELECT setval('users_id_seq', max(id)) FROM users;
SELECT setval('user_roles_id_seq', max(id)) FROM user_roles;