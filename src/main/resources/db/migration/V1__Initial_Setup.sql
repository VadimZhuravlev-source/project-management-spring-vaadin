DROP TABLE IF EXISTS "project_tasks";

CREATE TABLE project_tasks(
   id SERIAL NOT null,
   parent_id INT REFERENCES project_tasks,
   level_order INT NOT null,
   version INT,
   date_of_creation timestamp without time zone,
   update_date timestamp without time zone,

   name VARCHAR(150),
   start_date timestamp without time zone,
   finish_date timestamp without time zone,
   CONSTRAINT project_tasks_pkey PRIMARY KEY (id)
);

CREATE INDEX ON project_tasks(parent_id);