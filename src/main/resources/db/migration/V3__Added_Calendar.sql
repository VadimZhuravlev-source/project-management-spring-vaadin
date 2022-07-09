
ALTER TABLE project_tasks
DROP CONSTRAINT IF EXISTS fk_calendar_id;

ALTER TABLE project_tasks
DROP COLUMN IF EXISTS calendar_id;

DROP TABLE IF EXISTS "day_of_week_settings";
DROP TABLE IF EXISTS "calendars";

CREATE TABLE calendars(
   id SERIAL NOT null,
   version INT,
   name VARCHAR(50),
   setting VARCHAR(50),
   CONSTRAINT calendars_pkey PRIMARY KEY (id)
);

ALTER TABLE project_tasks
ADD calendar_id INT,
ADD CONSTRAINT fk_calendar_id
      FOREIGN KEY(calendar_id)
	  REFERENCES Calendars(id)
	  ON DELETE SET NULL;

CREATE TABLE day_of_week_settings(
   id SERIAL,
   version INT,
   calendar_id INT NOT NULL,
   day_of_week SMALLINT,
   count_hours NUMERIC(4, 2),
   CONSTRAINT day_of_week_settings_pkey PRIMARY KEY (id),
   CONSTRAINT fk_day_of_week_settings_calendars_id
      FOREIGN KEY(calendar_id)
      REFERENCES calendars(id)
      ON DELETE CASCADE
);