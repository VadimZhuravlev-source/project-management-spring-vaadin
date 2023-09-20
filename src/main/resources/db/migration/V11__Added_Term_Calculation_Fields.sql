-- time_unit
CREATE TABLE time_unit(
   id SERIAL,
   version INT,
   predefined BOOLEAN NOT NULL DEFAULT FALSE,
   name VARCHAR(150),
   number_of_hours NUMERIC(9, 2),
   CONSTRAINT time_unit_pkey PRIMARY KEY (id)
);

INSERT INTO time_unit(
    id, version, predefined, name, number_of_hours)
    VALUES (1, 1, TRUE, 'Day', 8),
           (2, 1, TRUE, '12 hours', 12),
           (3, 1, TRUE, '24 hours', 24),
           (4, 1, TRUE, 'Week', 40),
           (5, 1, TRUE, 'Month', 160),
           (6, 1, TRUE, 'Year', 2080);

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

ALTER TABLE calendars
ADD predefined BOOLEAN NOT NULL DEFAULT FALSE;

INSERT INTO calendars(id, version, predefined, name, start_time, settings_id)
VALUES(1, 0, TRUE, 'Standard','9:00:00', 0);
