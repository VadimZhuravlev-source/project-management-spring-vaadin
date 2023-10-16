--DROP FUNCTION get_all_dependencies(pairs TEXT);
DROP FUNCTION get_all_dependencies(pid INT, dependencies_ids INT[])

CREATE FUNCTION get_all_dependencies(pairs TEXT) RETURNS
	TABLE(checked_id INT, id INT, path TEXT, is_cycle BOOLEAN, link_id INT) AS
$BODY$
BEGIN
	RETURN QUERY
		WITH RECURSIVE
        -- parse text parameter
		pairs_text AS (
		SELECT 
			UNNEST(string_to_array(pairs, ';')) pair 
		),

		pairs_before_cast AS (
		SELECT 
			string_to_array(pair, '#') pair
		FROM pairs_text
		),

		pairs AS (
		SELECT 
			CAST(pair[1] AS INT) id,
			CAST(pair[2] AS INT[]) dependencies_ids
		FROM pairs_before_cast
		),
		
		--recursive query for obtain dependencies
		all_dependencies AS (
        SELECT
        	p.id checked_id,
			p.id id,
        	ARRAY[p.id] path,
        	ARRAY[p.id] || (pairs.dependencies_ids) checking_cycle_path,
        	FALSE is_cycle,
        	NULL::INT link_id,
        	FALSE complete_execution
        FROM
        	project_tasks p
		JOIN pairs
			ON p.id = pairs.id
			
        UNION ALL

        	(
        	WITH all_dependencies_inner AS (
        	SELECT
        		all_dependencies.checked_id,
				all_dependencies.id id,
        		all_dependencies.path,
        		all_dependencies.checking_cycle_path,
        		all_dependencies.is_cycle,
        		all_dependencies.link_id
        	FROM
        		all_dependencies
        	WHERE
        		NOT all_dependencies.complete_execution
        	),

        	dependencies AS (
        	SELECT
        		all_dependencies_inner.checked_id,
				project_tasks.parent_id id,
        		all_dependencies_inner.path || project_tasks.parent_id path,
        		all_dependencies_inner.checking_cycle_path || project_tasks.parent_id checking_cycle_path,
        		project_tasks.parent_id = ANY(all_dependencies_inner.checking_cycle_path) is_cycle,
        		NULL::INT AS link_id
        	FROM
        		all_dependencies_inner
        	JOIN
        		project_tasks
        		ON all_dependencies_inner.id = project_tasks.id
        			AND project_tasks.parent_id IS NOT NULL

        	UNION

        	SELECT
        		all_dependencies_inner.checked_id,
				links.project_task,
        		all_dependencies_inner.path || links.project_task path,
        		all_dependencies_inner.checking_cycle_path || links.project_task checking_cycle_path,
        		links.project_task = ANY(all_dependencies_inner.checking_cycle_path) is_cycle,
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

        SELECT DISTINCT
            all_dependencies.checked_id,
			all_dependencies.id,
            CASE WHEN all_dependencies.is_cycle
                THEN array_to_string(all_dependencies.path, ',')
                ELSE ''
            END path,
            all_dependencies.is_cycle,
            all_dependencies.link_id
        FROM all_dependencies;
		
END;
$BODY$

LANGUAGE plpgsql;