WITH RECURSIVE 
parents_of_parents AS (
SELECT 
	p.id,
	p.parent_id pid,
	0 level,
	ARRAY[id] path,
	FALSE is_cycle
FROM
	project_tasks p
WHERE
	p.id = 4101
	--p.id = ANY('{54242}') -- created cycle looping
	
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
		AND NOT pop.is_cycle
),

proceed_execution AS (
SELECT 
	NOT bool_or(p.is_cycle) proceed
FROM	
	parents_of_parents p	
),

task_predecessor AS (
SELECT 
	p.id
FROM
	proceed_execution
JOIN project_tasks p
	ON proceed_execution.proceed
		AND p.id = ANY('{4102}')
		--AND p.id = ANY('{4102,3618}') -- 3618 - id of parent task
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

proceed_execution2 AS (
SELECT 
	pe.proceed AND NOT pp.are_in proceed
FROM 
	proceed_execution pe,
	are_parents_in_predecessors pp 
),

hier AS (
		  SELECT
			pop.id pt_id,
			NULL::INT pt_link_id,
			ARRAY[id] path,
			FALSE is_cycle,
			NULL::INT AS link_id,
			TRUE first_iteration
			--,0 level
		  FROM
			parents_of_parents pop
		  JOIN proceed_execution2
			ON proceed_execution2.proceed
	
		UNION ALL
	
		  SELECT
			project_tasks.parent_id,
			links.project_task,
			CASE
				WHEN links.id IS NOT NULL AND project_tasks.parent_id IS NOT NULL
				THEN hier.path || links.project_task || project_tasks.parent_id			
				WHEN links.id IS NOT NULL 
				THEN hier.path || links.project_task
				WHEN project_tasks.parent_id IS NOT NULL 
				THEN hier.path || project_tasks.parent_id
			END, 
			CASE
				WHEN links.id IS NULL 
				THEN project_tasks.parent_id = ANY(hier.path) OR project_tasks.id = ANY('{4102}')
				ELSE links.project_task = ANY(hier.path) OR links.project_task = ANY('{4102}')
			END,
			links.id,
			FALSE
			--,hier.level + 1
		  FROM
			hier
		  LEFT JOIN links
			ON hier.pt_id = links.linked_project_task
				OR hier.pt_link_id = links.linked_project_task
		  LEFT JOIN project_tasks
			ON (hier.pt_id = project_tasks.id
				OR hier.pt_link_id = project_tasks.id)
				AND NOT hier.first_iteration
		  WHERE
			(links.project_task IS NOT NULL OR project_tasks.parent_id IS NOT NULL)
			AND NOT hier.is_cycle
			--AND hier.level < 20
)

select hier.*, p.name, p1.name from hier LEFT JOIN project_tasks p ON hier.pt_id = p.id LEFT JOIN project_tasks p1 ON hier.pt_link_id = p1.id;