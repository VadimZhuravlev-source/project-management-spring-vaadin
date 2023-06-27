--DROP FUNCTION get_all_dependencies(pairs TEXT);

with dependencies as(
SELECT
	dep.checked_id,
	dep.id,
	dep.path,
	dep.is_cycle,
	dep.link_id
FROM get_all_dependencies('3;4') dep
),

proceeding_exection AS (
SELECT 
	bool_or(dependencies.is_cycle) is_cycle
FROM dependencies
),

unique_ids AS (
SELECT DISTINCT
	dependencies.id	
FROM dependencies, proceeding_exection
WHERE 
	NOT proceeding_exection.is_cycle	
	
)

SELECT 
	unique_ids.id,
	NULL::INT link_id
FROM
	unique_ids
	
UNION

SELECT 
	project_tasks.id,
	NULL::INT
FROM
	unique_ids
JOIN project_tasks
	ON unique_ids.id = project_tasks.parent_id
	
UNION	
	
SELECT 
	NULL::INT,
	links.id
FROM
	unique_ids
JOIN links
	ON unique_ids.id = links.project_task

--select * from uniq_ids