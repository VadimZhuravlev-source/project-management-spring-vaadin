WITH all_children_ids AS (	
	SELECT id
	FROM get_children_in_depth_fast('{3619}')	
),

all_checked_ids AS (
SELECT DISTINCT 
	id
FROM all_children_ids

UNION
	
SELECT
	linked_project_task
FROM links 
WHERE 
	project_task = ANY( 
		ARRAY(SELECT 
			  id 
			  FROM all_children_ids
			 ) 
	)
	
)

SELECT 
 	dep.id,
 	array_to_string(dep.path, ',') path,
 	dep.is_cycle,
 	dep.link_id
FROM get_all_dependencies(3618, ARRAY(SELECT id FROM all_checked_ids)) dep
 