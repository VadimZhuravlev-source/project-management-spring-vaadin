WITH parents_of_allowed_tasks AS (

	SELECT
		parents.id id
	FROM 
		get_parents_in_depth('{3, 6}') parents

),

children_of_task AS (
	SELECT
		id
	FROM
		project_tasks
	WHERE
		parent_id IS NULL
),

allowed_tasks AS (
	
	SELECT 
		children_of_task.id
	FROM
		children_of_task
	JOIN parents_of_allowed_tasks
		ON children_of_task.id = parents_of_allowed_tasks.id
	
)

SELECT
	*
FROM
	allowed_tasks

/*
SELECT
	*
FROM 
	project_tasks
WHERE
	project_tasks.id IN(
	SELECT
		id
	FROM
		allowed_tasks
	)
*/