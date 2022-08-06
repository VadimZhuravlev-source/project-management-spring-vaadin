CREATE TABLE calendar_exception(
   id SERIAL NOT null,
   calendar_id INT,
   calendar_date DATE,
   CONSTRAINT calendar_exception_pkey PRIMARY KEY (id),
   CONSTRAINT fk_day_of_week_settings_calendars_id
         FOREIGN KEY(calendar_id)
         REFERENCES calendars(id)
         ON DELETE CASCADE
);