DROP FUNCTION IF EXISTS get_children_of_parent_in_depth(array_parent_id TEXT);
DROP FUNCTION IF EXISTS get_parents_of_parent(array_parent_id TEXT);
--DROP FUNCTION IF EXISTS get_links_in_depth(array_parent_id TEXT);

CREATE FUNCTION get_children_in_depth_fast(ids TEXT) RETURNS SETOF project_tasks AS
$BODY$
BEGIN
	RETURN QUERY
		WITH RECURSIVE
		hier AS (
		  SELECT
			ARRAY[project_tasks.id] id$
		  FROM
			project_tasks
		  WHERE
			project_tasks.id = ANY(ids::INT[])
		UNION ALL
		  SELECT
			ARRAY(
			  SELECT
                project_tasks.id
			  FROM
				project_tasks
			  WHERE
				project_tasks.parent_id = ANY(hier.id$)
				AND NOT (project_tasks.id = ANY(ids::INT[])) -- Protection from looping
			) id$
		  FROM
			hier
		  WHERE
			COALESCE(id$, '{}') <> '{}' -- loop exit condition - empty array
		),

		uniq_ids AS (
        SELECT DISTINCT
			UNNEST(id$) id
		FROM
			hier)

        SELECT
            project_tasks.*
        FROM project_tasks
        JOIN uniq_ids
            ON project_tasks.id = uniq_ids.id
        ORDER BY level_order ASC;
END;
$BODY$

LANGUAGE plpgsql;

CREATE FUNCTION get_parents_in_depth(ids TEXT) RETURNS SETOF project_tasks AS
$BODY$
BEGIN
	RETURN QUERY
		WITH RECURSIVE hier AS (
		  SELECT
			project_tasks.*
		  FROM
			project_tasks
		  WHERE
			project_tasks.id = ANY(ids::INT[])
		UNION ALL
		  SELECT
			p.*
		  FROM
			hier
		  JOIN project_tasks p
			ON p.id = hier.parent_id
			AND NOT (p.id = ANY(ids::INT[]))) -- Protection from looping

		SELECT DISTINCT * FROM hier;
END;
$BODY$

LANGUAGE plpgsql;

CREATE FUNCTION get_links_in_depth(p_t_ids TEXT) RETURNS SETOF links AS
$BODY$
BEGIN
	RETURN QUERY
		WITH RECURSIVE hier AS (
		  SELECT
			links.*
		  FROM
			links
		  WHERE
			links.project_task = ANY(p_t_ids::INT[])
		UNION ALL
		  SELECT
			l.*
		  FROM
			hier
		  JOIN links l
			ON l.project_task = hier.linked_project_task
			AND NOT (l.project_task = ANY(p_t_ids::INT[]))) -- Protection from looping

		SELECT DISTINCT * FROM hier;
END;
$BODY$

LANGUAGE plpgsql;

-- indices
DROP INDEX IF EXISTS links_linked_project_task_project_task_idx;
DROP INDEX IF EXISTS project_tasks_parent_id_idx;

CREATE INDEX ON links(id);
CREATE INDEX ON links(project_task, linked_project_task);

CREATE INDEX ON project_tasks(parent_id, id);
CREATE INDEX ON project_tasks(parent_id, level_order);



