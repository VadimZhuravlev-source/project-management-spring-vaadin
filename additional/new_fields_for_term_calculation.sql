ALTER TABLE project_tasks
ADD duration BIGINT NOT NULL DEFAULT 1;

ALTER TABLE project_tasks
ADD schedule_mode_id SMALLINT NOT NULL DEFAULT 0;

ALTER TABLE project_tasks
ADD is_project BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE links
ADD 'lag' BIGINT NOT NULL DEFAULT 0;


ALTER TABLE links
-- time unit
ADD calendar_id INT,
ADD CONSTRAINT fk_calendar_id
      FOREIGN KEY(calendar_id)
	  REFERENCES Calendars(id)
	  ON DELETE SET NULL;


ALTER TABLE day_of_week_settings
DROP COLUMN IF EXISTS count_hours;

ALTER TABLE day_of_week_settings
ADD COLUMN duration INT NOT NULL DEFAULT 1;

ALTER TABLE calendar_exception
DROP COLUMN IF EXISTS duration;

ALTER TABLE calendar_exception
ADD COLUMN duration INT NOT NULL DEFAULT 1;

ALTER TABLE calendars
ADD COLUMN start_time NUMERIC(4, 2);

ALTER TABLE calendars
DROP COLUMN IF EXISTS settings;
ALTER TABLE calendars
ADD settings_id SMALLINT NOT NULL DEFAULT 0;
