--DROP FUNCTION IF EXISTS get_children_of_parent_in_depth(array_parent_id TEXT);
--DROP FUNCTION IF EXISTS get_parents_of_parent(array_parent_id TEXT);

CREATE FUNCTION get_children_of_parent_in_depth(array_parent_id TEXT) RETURNS SETOF project_tasks AS
$BODY$
BEGIN
	RETURN QUERY
		WITH RECURSIVE hier AS (
		  SELECT
			ARRAY[project_tasks.id] id$
		  FROM
			project_tasks
		  WHERE
			project_tasks.id = ANY(array_parent_id::INT[])
		UNION ALL
		  SELECT
			ARRAY(
			  SELECT
                project_tasks.id
			  FROM
				project_tasks
			  WHERE
				project_tasks.parent_id = ANY(hier.id$)
			) id$
		  FROM
			hier
		  WHERE
			COALESCE(id$, '{}') <> '{}' -- условие выхода из цикла - пустой массив
		),

		full_hierarchy_of_element AS (
        SELECT DISTINCT
			UNNEST(id$) id
		FROM
			hier)

        SELECT
            project_tasks.*
        FROM project_tasks
        JOIN full_hierarchy_of_element
            ON project_tasks.id = full_hierarchy_of_element.id
        ORDER BY level_order ASC;
END;
$BODY$

LANGUAGE plpgsql;

CREATE FUNCTION get_parents_of_parent(array_parent_id TEXT) RETURNS SETOF project_tasks AS
$BODY$
BEGIN
	RETURN QUERY
		WITH RECURSIVE hier AS (
		  SELECT
			project_tasks.*
		  FROM
			project_tasks
		  WHERE
			project_tasks.id = ANY(array_parent_id::INT[])
		UNION ALL
		  SELECT
			p.*
		  FROM
			hier
		  join project_tasks p
			on p.id = hier.parent_id)
		SELECT DISTINCT * FROM hier;
END;
$BODY$

LANGUAGE plpgsql