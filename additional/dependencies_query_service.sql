WITH AllChildren AS (
	SELECT *
	FROM get_children_in_depth_fast(:checkedIds)
	
	
)

SELECT 
	dep.id,
	array_to_string(dep.path, ',') path,
	dep.is_cycle,
	dep.link_id
FROM get_all_dependencies(:pid, :checkedIds) dep