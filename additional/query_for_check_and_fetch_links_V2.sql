WITH RECURSIVE 
all_dependencies AS (
SELECT
	p.id id,
	ARRAY[p.id] || ('{4102}') path,
	FALSE is_cycle,
	NULL::INT AS link_id,
	FALSE complete_execution
FROM
	project_tasks p
WHERE
	p.id = 4101
UNION ALL
	
	(
	WITH all_dependencies_inner AS (
	SELECT
		all_dependencies.id id,
		all_dependencies.path,
		all_dependencies.is_cycle,
		all_dependencies.link_id
	FROM
		all_dependencies
	WHERE 
		NOT all_dependencies.complete_execution
	),
			  
	dependencies AS (
	SELECT
		project_tasks.parent_id id,
		all_dependencies_inner.path || project_tasks.parent_id,
		project_tasks.parent_id = ANY(all_dependencies_inner.path) is_cycle,
		NULL::INT AS link_id
	FROM
		all_dependencies_inner
	JOIN
		project_tasks
		ON all_dependencies_inner.id = project_tasks.id
			AND project_tasks.parent_id IS NOT NULL
			  
	UNION
			  
	SELECT
		links.project_task,
		all_dependencies_inner.path || links.project_task,
		links.project_task = ANY(all_dependencies_inner.path) is_cycle,
		links.id AS link_id
	FROM
		all_dependencies_inner
	JOIN
		links
		ON all_dependencies_inner.id = links.linked_project_task  	
	),
			  
	check_proceeded_execution AS (
	SELECT 
		bool_or(dependencies.is_cycle) is_cycle
	FROM dependencies
	),
			  
	iterated_dependencies AS (
	SELECT
		dependencies.*,
		check_proceeded_execution.is_cycle complete_execution
	FROM
		dependencies,check_proceeded_execution
	)
		
	select * from iterated_dependencies
		
	)
	
)
select * from all_dependencies
--select hier.*, p.name, p1.name from hier LEFT JOIN project_tasks p ON hier.pt_id = p.id LEFT JOIN project_tasks p1 ON hier.pt_link_id = p1.id;