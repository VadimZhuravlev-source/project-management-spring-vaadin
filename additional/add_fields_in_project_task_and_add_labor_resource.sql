ALTER TABLE project_tasks
DROP COLUMN IF EXISTS status_id;

ALTER TABLE project_tasks
DROP COLUMN IF EXISTS progress;

ALTER TABLE project_tasks
DROP COLUMN IF EXISTS is_milestone;

ALTER TABLE project_tasks
ADD status_id SMALLINT NOT NULL DEFAULT 0;

ALTER TABLE project_tasks
ADD progress SMALLINT DEFAULT 0;

ALTER TABLE project_tasks
ADD is_milestone BOOLEAN NOT NULL DEFAULT FALSE;

DROP TABLE IF EXISTS "labor_resources";
CREATE TABLE labor_resources(
	id SERIAL,
	version INT,
	name VARCHAR(150),
	CONSTRAINT labor_resources_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "task_labor_resources";
CREATE TABLE task_labor_resources(
	id SERIAL,
	version INT,
	project_task_id INT NOT NULL,
	resource_id INT NOT NULL,
	duration NUMERIC(9, 2),
	sort INT NOT NULL,
	CONSTRAINT task_labor_resources_pkey PRIMARY KEY (id),
	CONSTRAINT fk_task_labor_resource_project_task_id
		FOREIGN KEY(project_task_id)
		REFERENCES project_tasks(id)
		ON DELETE CASCADE,
	CONSTRAINT fk_task_labor_resource_resource_id
		FOREIGN KEY(resource_id)
		REFERENCES labor_resources(id)
		ON DELETE SET NULL
);