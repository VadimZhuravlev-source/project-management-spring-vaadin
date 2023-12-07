WITH RECURSIVE tasks_that_used_calendar AS(
	SELECT 
		id,
		parent_id pid,
		ARRAY[id] path,
		is_project
	FROM project_tasks 
	WHERE 
		calendar_id = 1
	
	UNION ALL
	
	SELECT
		p.id,
		p.parent_id,
		tasks_that_used_calendar.path || p.parent_id path,
		p.is_project
	FROM
		tasks_that_used_calendar
	JOIN project_tasks p
		ON p.id = tasks_that_used_calendar.pid
			AND NOT (p.parent_id = ANY(tasks_that_used_calendar.path))
	
)
SELECT DISTINCT
		p.*	
	FROM	
		tasks_that_used_calendar
	JOIN project_tasks p
		ON p.id = tasks_that_used_calendar.id
	WHERE
		tasks_that_used_calendar.is_project OR tasks_that_used_calendar.pid IS NULL
