-- time_unit
CREATE TABLE time_unit(
   id SERIAL,
   version INT,
   predefined BOOLEAN NOT NULL DEFAULT FALSE,
   name VARCHAR(150),
   number_of_hours NUMERIC(5, 2),
   CONSTRAINT time_unit_pkey PRIMARY KEY (id)
);

INSERT INTO time_unit(
    id, version, predefined, name, number_of_hours)
    VALUES (1, 0, TRUE, 'day', 8);

-- project_tasks
ALTER TABLE project_tasks
ADD duration BIGINT NOT NULL DEFAULT 1;

ALTER TABLE project_tasks
ADD schedule_mode_id SMALLINT NOT NULL DEFAULT 0;

ALTER TABLE project_tasks
ADD is_project BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE project_tasks
ADD time_unit_id INT DEFAULT 1,
ADD CONSTRAINT fk_time_unit_id
      FOREIGN KEY(time_unit_id)
	  REFERENCES time_unit(id)
	  ON DELETE SET NULL;

-- links
ALTER TABLE links
ADD lag BIGINT NOT NULL DEFAULT 0;

ALTER TABLE links
ADD time_unit_id INT DEFAULT 1,
ADD CONSTRAINT fk_time_unit_id
      FOREIGN KEY(time_unit_id)
	  REFERENCES time_unit(id)
	  ON DELETE SET NULL;

-- day_of_week_settings
ALTER TABLE day_of_week_settings
DROP COLUMN IF EXISTS count_hours;

ALTER TABLE day_of_week_settings
ADD COLUMN duration INT NOT NULL DEFAULT 1;

-- calendar_exception
ALTER TABLE calendar_exception
DROP COLUMN IF EXISTS duration;

ALTER TABLE calendar_exception
ADD COLUMN duration INT NOT NULL DEFAULT 1;

-- calendars
ALTER TABLE calendars
ADD COLUMN start_time TIME;

ALTER TABLE calendars
DROP COLUMN IF EXISTS setting;
ALTER TABLE calendars
ADD settings_id SMALLINT NOT NULL DEFAULT 0;
