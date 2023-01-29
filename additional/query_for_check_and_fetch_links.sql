WITH RECURSIVE parents_of_parents AS (
SELECT 
	p.id,
	p.parent_id pid,
	0 level,
	ARRAY[id] path,
	FALSE circle
FROM
	project_tasks p
WHERE
	p.id = ANY('{4101}')
	--p.id = ANY('{54242}') -- created circle looping
	
UNION ALL

SELECT 
	p.id,
	p.parent_id pid,
	pop.level + 1,
	pop.path || p.id,
	p.id = ANY(pop.path)
FROM
	parents_of_parents pop
JOIN project_tasks p
	ON pop.pid = p.id
		AND NOT pop.circle
),

proceed_excution AS (
SELECT 
	NOT bool_or(p.circle) proceed
FROM	
	parents_of_parents p	
),

task_predecessor AS (
SELECT 
	p.id
FROM
	proceed_excution
JOIN project_tasks p
	ON proceed_excution.proceed
		AND p.id = ANY('{4102}')
		--AND p.id = ANY('{4102,3618}') -- 3618 - id of parent task
/*WHERE
	CASE 
		WHEN proceed_excution.proceed 
		THEN p.id = ANY('{4102}')
		ELSE FALSE
	END
*/
),

--if the task's predecessors refer to one of the task's parents, then a collision occurs
are_parents_in_predecessors AS (
SELECT 
	COUNT(tp.id) > 0 are_in
FROM 
	task_predecessor tp
JOIN parents_of_parents pop
	ON tp.id = POP.id
),

proceed_excution2 AS (
SELECT 
	pe.proceed AND NOT pp.are_in proceed
FROM 
	proceed_excution pe,
	are_parents_in_predecessors pp 
),

hier AS (
		  SELECT
			pop.id,
			ANY('{4102}') checked_predecessors,
			pop.path path,
			FALSE circle,
			NULL link_id,
			TRUE first_iteration
		  FROM
			parents_of_parents pop
		  JOIN procees_excution2
			ON proceed_execution2.proceed
	
		UNION ALL
	
		  SELECT
			CASE
				WHEN links.id = NULL THEN project_tasks.id ELSE links.project_task
			END,		
			hier.checked_predecessors,
			CASE
				WHEN links.id = NULL THEN hier.path || project_tasks.id ELSE hier.path || links.project_task
			END, 
			CASE
				WHEN links.id = NULL THEN project_tasks.id ELSE links.project_task
			END		
			CASE
				WHEN links.id = NULL 
				THEN project_tasks.id = ANY(pop.path) || project_tasks.id = ANY(hier.checked_predecessors)
				ELSE links.project_task = ANY(pop.path) || links.project_task = ANY(hier.checked_predecessors)
			END,
			links.id,
			FALSE
		  FROM
			hier
		  LEFT JOIN links
			ON links.linked_project_task = hier.id
				AND NOT hier.circle
		  LEFT JOIN project_tasks
			ON project_tasks.parent_id = hier.id
				AND NOT hier.circle
				AND NOT hier.first_iteration

select * from proceed_excution2;