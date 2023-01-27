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
	p.id = ANY('{54242}') -- created circle looping
	
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

predecessor AS (
SELECT 
	p.id
FROM
	proceed_excution
JOIN project_tasks p
	ON proceed_excution.proceed
		AND p.id = ANY('{4102}')
/*WHERE
	CASE 
		WHEN proceed_excution.proceed 
		THEN p.id = ANY('{4102}')
		ELSE FALSE
	END
*/
)

select * from predecessor;