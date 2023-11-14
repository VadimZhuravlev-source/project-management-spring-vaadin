-- Exceptions
CREATE TABLE calendar_exceptions(
	id SERIAL,
	version INT,
	calendar_id INT NOT NULL,
	name VARCHAR(150),
	setting_id SMALLINT NOT NULL DEFAULT 0,
	start DATE,
	finish DATE,
	finish_after INT DEFAULT 1,
	sort INT,
	pattern_id SMALLINT NOT NULL DEFAULT 0,
	number_of_days INT DEFAULT 1,
	number_of_weeks INT DEFAULT 1,
	every_monday NOT NULL DEFAULT FALSE,
	every_tuesday NOT NULL DEFAULT FALSE,
	every_wednesday NOT NULL DEFAULT FALSE,
	every_thursday NOT NULL DEFAULT FALSE,
	every_friday NOT NULL DEFAULT FALSE,
	every_saturday NOT NULL DEFAULT FALSE,
	every_sunday NOT NULL DEFAULT FALSE,
	
	CONSTRAINT calendar_exceptions_pkey PRIMARY KEY (id),
	CONSTRAINT fk_calendar_exceptions_calendars_id
		FOREIGN KEY(calendar_id)
		REFERENCES calendars(id)
		ON DELETE CASCADE
);

CREATE TABLE exception_intervals(
   id SERIAL,
   version INT,
   calendar_exception_id INT NOT NULL,
   from_time TIME,
   to_time TIME,
   sort INT,
   CONSTRAINT exception_intervals_pkey PRIMARY KEY (id),
   CONSTRAINT fk_exception_intervals_calendar_exceptions_id
      FOREIGN KEY(calendar_exception_id)
      REFERENCES calendar_exceptions(id)
      ON DELETE CASCADE
);

-- Working week
CREATE TABLE working_weeks(
   id SERIAL,
   version INT,
   calendar_id INT NOT NULL,
   name VARCHAR(150),
   start DATE,
   finish DATE,
   sort INT,
   CONSTRAINT working_weeks_pkey PRIMARY KEY (id),
   CONSTRAINT fk_working_weeks_calendars_id
      FOREIGN KEY(calendar_id)
      REFERENCES calendars(id)
      ON DELETE CASCADE
);

CREATE TABLE working_times(
   id SERIAL,
   version INT,
   working_week_id INT NOT NULL,
   day_of_week SMALLINT NOT NULL DEFAULT 1,
   interval_setting_id SMALLINT NOT NULL DEFAULT 1,
   CONSTRAINT working_times_pkey PRIMARY KEY (id),
   CONSTRAINT fk_working_times_working_weeks_id
      FOREIGN KEY(working_week_id)
      REFERENCES working_weeks(id)
      ON DELETE CASCADE
);

CREATE TABLE working_time_intervals(
   id SERIAL,
   version INT,
   working_time_id INT NOT NULL,
   from_time TIME,
   to_time TIME,
   sort INT,
   CONSTRAINT working_time_intervals_pkey PRIMARY KEY (id),
   CONSTRAINT fk_working_time_intervals_working_times_id
      FOREIGN KEY(working_time_id)
      REFERENCES working_times(id)
      ON DELETE CASCADE
);
