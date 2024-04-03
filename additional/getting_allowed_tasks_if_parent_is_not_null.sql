WITH is_current_task_child_of_allowed_task AS (

	SELECT
		bool_or(TRUE) allowed_all
	FROM get_parents_in_depth('{3}')
	WHERE id = ANY('{2}')

),

parents_of_allowed_tasks AS (

	SELECT
		parents.id id
	FROM 
		is_current_task_child_of_allowed_task
	JOIN get_parents_in_depth('{2}') parents
		ON NOT is_current_task_child_of_allowed_task.allowed_all 

),

children_of_task AS (
	SELECT
		id
	FROM
		project_tasks
	WHERE
		parent_id = ANY('{3}')
),

allowed_tasks AS (
	SELECT DISTINCT
		id
	FROM
		children_of_task
	JOIN is_current_task_child_of_allowed_task
		ON is_current_task_child_of_allowed_task.allowed_all
	
	UNION ALL
	
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