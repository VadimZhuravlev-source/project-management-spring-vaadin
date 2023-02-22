DROP FUNCTION IF EXISTS get_children_of_parent_in_depth(array_parent_id TEXT);
DROP FUNCTION IF EXISTS get_parents_of_parent(array_parent_id TEXT);
--DROP FUNCTION IF EXISTS get_all_dependencies(pid INT, linked_p_t_ids TEXT);

CREATE FUNCTION get_children_in_depth_fast(ids INT[]) RETURNS TABLE(id INT) AS
$BODY$
BEGIN
	RETURN QUERY
		WITH RECURSIVE
		hierarchy AS (
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
				project_tasks.parent_id = ANY(hierarchy.id$)
				AND NOT (project_tasks.id = ANY(ids::INT[])) -- Protection from looping
			) id$
		  FROM
			hierarchy
		  WHERE
			COALESCE(id$, '{}') <> '{}' -- loop exit condition - empty array
		)

		SELECT DISTINCT
			UNNEST(id$) id
		FROM
			hierarchy;

END;
$BODY$

LANGUAGE plpgsql;

CREATE FUNCTION get_parents_in_depth(ids INT[]) RETURNS TABLE(id INT) AS
$BODY$
BEGIN
	RETURN QUERY
		WITH RECURSIVE hierarchy AS (
		  SELECT
			project_tasks.parent_id id
		  FROM
			project_tasks
		  WHERE
			project_tasks.id = ANY(ids::INT[])
		UNION ALL
		  SELECT
			p.parent_id
		  FROM
			hierarchy
		  JOIN project_tasks p
			ON p.id = hierarchy.id
			AND NOT (p.id = ANY(ids::INT[]))) -- Protection from looping

		SELECT DISTINCT * FROM hierarchy;
END;
$BODY$

LANGUAGE plpgsql;

CREATE FUNCTION get_all_dependencies(pid INT, dependencies_ids INT[]) RETURNS
	TABLE(id INT, path INT[], is_cycle BOOLEAN, link_id INT, complete_execution BOOLEAN) AS
$BODY$
BEGIN
	RETURN QUERY
		WITH RECURSIVE
        all_dependencies AS (
        SELECT
        	p.id id,
        	ARRAY[p.id] || (dependencies_ids::INT[]) path,
        	FALSE is_cycle,
        	NULL::INT AS link_id,
        	FALSE complete_execution
        FROM
        	project_tasks p
        WHERE
        	p.id = pid
        UNION ALL

        	(
        	WITH all_dependencies_inner AS (
        	SELECT
        		all_dependencies.id id,
        		all_dependencies.path,
        		all_dependencies.is_cycle,
        		all_dependencies.link_id
        	FROM
        		all_dependencies
        	WHERE
        		NOT all_dependencies.complete_execution
        	),

        	dependencies AS (
        	SELECT
        		project_tasks.parent_id id,
        		all_dependencies_inner.path || project_tasks.parent_id,
        		project_tasks.parent_id = ANY(all_dependencies_inner.path) is_cycle,
        		NULL::INT AS link_id
        	FROM
        		all_dependencies_inner
        	JOIN
        		project_tasks
        		ON all_dependencies_inner.id = project_tasks.id
        			AND project_tasks.parent_id IS NOT NULL

        	UNION

        	SELECT
        		links.project_task,
        		all_dependencies_inner.path || links.project_task,
        		links.project_task = ANY(all_dependencies_inner.path) is_cycle,
        		links.id AS link_id
        	FROM
        		all_dependencies_inner
        	JOIN
        		links
        		ON all_dependencies_inner.id = links.linked_project_task
        	),

        	check_proceeded_execution AS (
        	SELECT
        		bool_or(dependencies.is_cycle) is_cycle
        	FROM dependencies
        	),

        	iterated_dependencies AS (
        	SELECT
        		dependencies.*,
        		check_proceeded_execution.is_cycle complete_execution
        	FROM
        		dependencies,check_proceeded_execution
        	)

        	SELECT * FROM iterated_dependencies

        	)

        )

        SELECT DISTINCT * FROM all_dependencies;

END;
$BODY$

LANGUAGE plpgsql;

-- indices
DROP INDEX IF EXISTS links_linked_project_task_project_task_idx;
DROP INDEX IF EXISTS project_tasks_parent_id_idx;

CREATE INDEX ON links(id);
CREATE INDEX ON links(project_task, linked_project_task);
CREATE INDEX ON links(linked_project_task, project_task, id);

CREATE INDEX ON project_tasks(id, parent_id);
CREATE INDEX ON project_tasks(parent_id, id);
CREATE INDEX ON project_tasks(parent_id, level_order);
