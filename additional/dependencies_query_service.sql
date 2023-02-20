WITH all_children_ids AS (
	
	SELECT id
	FROM get_children_in_depth_fast('{3619}')
	
),

all_checked_ids AS (
select distinct 
	id
from all_children_ids

union
	
select
	linked_project_task from links where project_task = any( array(select id from all_children_ids) )
	
)

--select * from all_children_ids

  
 SELECT 
 	dep.id,
 	array_to_string(dep.path, ',') path,
 	dep.is_cycle,
 	dep.link_id
 FROM get_all_dependencies(3618, '{' + array_to_string(array(select id from all_checked_ids) + '}', ',', '')) dep
 