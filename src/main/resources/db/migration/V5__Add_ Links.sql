--DROP TABLE IF EXISTS "links";

CREATE TABLE links(
   id SERIAL NOT null,
   version INT,
   row_order INT NOT null,
   project_task INT REFERENCES project_tasks,
   linked_project_task INT REFERENCES project_tasks,
   link_type smallint,
   CONSTRAINT links_pkey PRIMARY KEY (project_task, row_order, id)
);

CREATE INDEX ON links(linked_project_task, project_task);