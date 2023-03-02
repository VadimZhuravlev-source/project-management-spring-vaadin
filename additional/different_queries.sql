
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
	--p.id = ANY('{4101}')
	p.id = ANY('{54242}')

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
	NOT bool_or(p.circle)
FROM
	parents_of_parents p
),

precedessor AS (
SELECT
	unnest(ANY('{54242}'))

)

select * from precedessor;

--------------------------------------------

--select * from project_tasks where name like '%1.2.3.1'
--select * from links where project_task = 4102
select * from project_tasks where id = 244





--select * from links;
--select count(id) from project_tasks
--select get_children_in_depth_fast('{25}')
--select * from project_tasks LIMIT 1;

/*
INSERT INTO project_tasks (version, name, level_order)
VALUES (0, 'cycle looping 1', 2),
(0, 'cycle looping 2', 1),
(0, 'cycle looping 3', 2),
(0, 'cycle looping 4', 3)
RETURNING *;

UPDATE project_tasks
SET parent_id = 54242
WHERE id = 54243;
UPDATE project_tasks
SET parent_id = 54243
WHERE id = 54244;
UPDATE project_tasks
SET parent_id = 54244
WHERE id = 54245;
UPDATE project_tasks
SET parent_id = 54245
WHERE id = 54242;
*/

select * from project_tasks where name like 'cycle looping %';

-- еще одна вкладка

WITH RECURSIVE parents_of_parents AS (
SELECT
	p.id,
	p.parent_id pid
	0 level
FROM
	project_tasks p
WHERE
	p.id = ANY('{4101}')

UNION ALL

SELECT
	p.id,
	p.parent_id pid,
	parents_of_parents.level + 1
FROM
	parents_of_parents
JOIN project_tasks p
	ON parents_of_parents.pid = p.id

),


hier AS (
		 -- Need to get all parent of current PT in depth and to pass its in the function
		  SELECT
			links.project_task pid,
			links.linked_project_task id,
			FALSE itProjectTask,
			0 level
		  FROM
			links
		  WHERE
			links.project_task = ANY('{10}')
		UNION ALL
		  SELECT
			CASE
				WHEN l.project_task = NULL THEN PT.parent_id ELSE l.project_task
			END,
			CASE
				WHEN l.project_task = NULL THEN PT.id ELSE l.linked_project_task
			END,
			CASE
				WHEN l.project_task = NULL THEN TRUE ELSE FALSE
			END,
			hier.level + 1

		  FROM
			hier
		  LEFT JOIN links l
			ON hier.id = l.project_task
			--AND NOT (l.project_task = ANY('{10}'))
		  LEFT JOIN project_tasks pt
			ON hier.id = pt.parent_id
		  WHERE
			NOT (l.project_task = ANY('{10}'))
) -- Protection from looping


		SELECT DISTINCT * FROM hier;