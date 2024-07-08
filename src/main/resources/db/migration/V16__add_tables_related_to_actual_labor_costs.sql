DROP TABLE IF EXISTS "user_labor_resources";
DROP TABLE IF EXISTS "labor_cost_intervals";
DROP TABLE IF EXISTS "labor_costs";
CREATE TABLE labor_costs(
	id SERIAL,
	version INT,
	name VARCHAR(150),
	date_of_creation timestamp without time zone,
   	update_date timestamp without time zone,
	labor_resource_id INT,
	day date,
	CONSTRAINT labor_costs_pkey PRIMARY KEY (id),
	CONSTRAINT fk_labor_cost_labor_labor_resource_id
		FOREIGN KEY(labor_resource_id)
		REFERENCES labor_resources(id)
		ON DELETE SET NULL
);

CREATE TABLE labor_cost_intervals(
	id SERIAL,
	version INT,
	labor_cost_id INT NOT NULL,
	project_task_id INT NOT NULL,
	from_time TIME,
	to_time TIME,
	duration INT DEFAULT 0,
	CONSTRAINT labor_cost_intervals_pkey PRIMARY KEY (labor_cost_id, id),
	CONSTRAINT fk_labor_cost_interval_labor_cost_id
		FOREIGN KEY(labor_cost_id)
		REFERENCES labor_costs(id)
		ON DELETE CASCADE,
	CONSTRAINT fk_labor_cost_interval_project_task_id
		FOREIGN KEY(project_task_id)
		REFERENCES project_tasks(id)
		ON DELETE CASCADE
);

CREATE TABLE user_labor_resources(
	id SERIAL,
	version INT,
	user_id INT NOT NULL,
	labor_resource_id INT NOT NULL,
	sort INT DEFAULT 0 NOT NULL,
	CONSTRAINT user_labor_resources_pkey PRIMARY KEY (user_id, id),
	CONSTRAINT fk_user_labor_resource_user_id
		FOREIGN KEY(user_id)
		REFERENCES users(id)
		ON DELETE CASCADE,
	CONSTRAINT fk_user_labor_resource_labor_resource_id
		FOREIGN KEY(labor_resource_id)
		REFERENCES labor_resources(id)
		ON DELETE CASCADE
);