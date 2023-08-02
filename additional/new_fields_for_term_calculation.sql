ALTER TABLE project_tasks
ADD duration BIGINT NOT NULL DEFAULT 1;

ALTER TABLE project_tasks
ADD schedule_mode_id SMALLINT NOT NULL DEFAULT 0;

ALTER TABLE project_tasks
ADD is_project BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE links
ADD delay NUMERIC(10, 2);


