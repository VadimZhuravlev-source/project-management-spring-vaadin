
WITH RECURSIVE found_ids AS (
	
	SELECT
		project_tasks.id id
		--calendar representation
		,project_tasks.calendar_id
		-- time unit representation
		,project_tasks.time_unit_id
		-- duration representation
		,project_tasks.duration
	FROM 
		project_tasks
	WHERE
		--&condition
		project_tasks.parent_id IS NULL
		--project_tasks.parent_id = ANY('{2}')
	),
	
	-- amount of children
	amount_of_children AS (
	SELECT
		found_ids.id id,
		COUNT(project_tasks.id) amount
	FROM 
		found_ids
	LEFT JOIN project_tasks
		ON found_ids.id = project_tasks.parent_id
	GROUP BY 
		found_ids.id
	),
	
	--calendar representation
	calendar_representation AS (
	SELECT
		calendars.id,
		calendars.name representation
	FROM
		calendars
	WHERE
		calendars.id IN (
			SELECT DISTINCT 
				calendar_id 
			FROM found_ids
		)	
	),
	
	-- time unit representation
	time_unit_representation AS (
	SELECT
		time_unit.id,
		time_unit.name representation,
		time_unit.number_of_hours
	FROM
		time_unit
	WHERE
		time_unit.id IN (
			SELECT DISTINCT 
				time_unit_id 
			FROM found_ids
		)	
	),
	
	-- duration representation
	durations AS (
	SELECT
		found_ids.id,
		CASE 
			WHEN (time_unit_representation.number_of_hours <> 0 AND NOT time_unit_representation.number_of_hours IS NULL)
			THEN found_ids.duration / (time_unit_representation.number_of_hours * 3600) 
			ELSE 0
		END duration
	FROM
		found_ids
	JOIN time_unit_representation
		ON found_ids.time_unit_id = time_unit_representation.id
	),
	
	-- links representation
	links_initial_recursive AS (
	SELECT	
		links.project_task,
		links.link_type,
		links.linked_project_task id,
		project_tasks.parent_id,
		CAST(project_tasks.level_order AS TEXT) wbs
	FROM
		links
	JOIN project_tasks
		ON links.linked_project_task = project_tasks.id
	WHERE
		links.project_task IN (
			SELECT
				id
			FROM found_ids
		)
	),
	
	found_links_wbs AS (
	
	SELECT
		links.project_task,
		links.link_type,
		links.id,
		links.parent_id,
		ARRAY[links.id] path,
		FALSE is_cycle,
		links.wbs
	FROM
		links_initial_recursive links
	
	UNION ALL
	
	SELECT
		found_links_wbs.project_task,
		found_links_wbs.link_type,
		found_links_wbs.id,
		project_tasks.parent_id,
		found_links_wbs.path || project_tasks.id path,
		project_tasks.id = ANY(found_links_wbs.path) is_cycle,
		project_tasks.level_order || '.' || found_links_wbs.wbs
	FROM
		found_links_wbs
	JOIN project_tasks
		ON found_links_wbs.parent_id = project_tasks.id
			AND NOT found_links_wbs.is_cycle
	
	),
	
	found_wbs AS (
	SELECT
		project_task id,
		--id,
		STRING_AGG(wbs || '-' || link_type, '; ') wbs
	FROM found_links_wbs
	WHERE 
		found_links_wbs.parent_id IS NULL
	GROUP BY
		project_task
	),
	
	-- result data
	results AS (
	SELECT
		found_ids.id,
		amount_of_children.amount
		,calendar_representation.representation calendar_rep
		,time_unit_representation.representation time_rep
		,durations.duration
		,found_wbs.wbs
	FROM	
		found_ids
	LEFT JOIN amount_of_children
		ON found_ids.id = amount_of_children.id
	LEFT JOIN calendar_representation
		ON found_ids.calendar_id = calendar_representation.id
	LEFT JOIN time_unit_representation
		ON found_ids.time_unit_id = time_unit_representation.id
	LEFT JOIN durations
		ON found_ids.id = durations.id
	LEFT JOIN found_wbs
		ON found_ids.id = found_wbs.id
	)
	
	SELECT * FROM results
	
/*
WITH found_task AS (
            SELECT parent_id, level_order FROM project_tasks WHERE id = 6
            )
            SELECT project_tasks.*
            FROM project_tasks
            	JOIN found_task
                    ON CASE WHEN found_task.parent_id IS NULL
                        THEN project_tasks.parent_id IS NULL
                        ELSE project_tasks.parent_id = found_task.parent_id
                    END
            WHERE
            	project_tasks.id NOT IN({})
            	AND project_tasks.level_order > found_task.level_order
            ORDER BY
            	project_tasks.level_order
*/