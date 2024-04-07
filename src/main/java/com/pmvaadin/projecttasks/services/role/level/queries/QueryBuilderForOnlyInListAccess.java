package com.pmvaadin.projecttasks.services.role.level.queries;

public class QueryBuilderForOnlyInListAccess {

    private final String nameOfAllowedTasks = "&allowedTasks";
    private final String nameOfFinalSelect = "&finalSelect";

    public String getQueryTextForUpperLevel(boolean isCount, String ids) {

        var queryText = getQueryTextForUpperLevel();
        var fillingOfFinalSelect = getFinalSelect(isCount);
        queryText = queryText.replace(nameOfFinalSelect, fillingOfFinalSelect);

        return queryText.replace(nameOfAllowedTasks, ids);

    }

    public String getQueryTextForParent(boolean isCount, String ids, String parentId) {

        var queryText = getQueryTextForChildrenOfParent();
        var fillingOfFinalSelect = getFinalSelect(isCount);
        queryText = queryText.replace(nameOfFinalSelect, fillingOfFinalSelect);
        var parent = "'{" + parentId + "}'";
        queryText = queryText.replace("&parentId", parent);

        return queryText.replace(nameOfAllowedTasks, ids);

    }

    private String getQueryTextForChildrenOfParent() {

        return """
                WITH is_current_task_child_of_allowed_task AS (
                                
                	SELECT
                		CASE WHEN bool_or(TRUE) IS NULL
                                               THEN FALSE
                                               ELSE TRUE
                                           END allowed_all
                	FROM get_parents_in_depth(&parentId)
                	WHERE id = ANY(&allowedTasks)
                                
                ),
                                
                parents_of_allowed_tasks AS (
                                
                	SELECT
                		parents.id id
                	FROM
                		is_current_task_child_of_allowed_task
                	JOIN get_parents_in_depth(&allowedTasks) parents
                		ON NOT is_current_task_child_of_allowed_task.allowed_all
                                
                ),
                                
                children_of_task AS (
                	SELECT
                		id
                	FROM
                		project_tasks
                	WHERE
                		parent_id = ANY(&parentId)
                ),
                                
                allowed_tasks AS (
                	SELECT DISTINCT
                		id
                	FROM
                		children_of_task
                	JOIN is_current_task_child_of_allowed_task
                		ON is_current_task_child_of_allowed_task.allowed_all
                	
                	UNION ALL
                	
                	SELECT
                		children_of_task.id
                	FROM
                		children_of_task
                	JOIN parents_of_allowed_tasks
                		ON children_of_task.id = parents_of_allowed_tasks.id
                	
                )
                                
                &finalSelect
                """;

    }

    private String getQueryTextForUpperLevel() {
        return
                """
                                                
                        WITH parents_of_allowed_tasks AS (
                                                
                        	SELECT
                        		parents.id id
                        	FROM
                        		get_parents_in_depth(&allowedTasks) parents
                                                
                        ),
                                                
                        children_of_task AS (
                        	SELECT
                        		id
                        	FROM
                        		project_tasks
                        	WHERE
                        		parent_id IS NULL
                        ),
                                                
                        allowed_tasks AS (
                        	
                        	SELECT
                        		children_of_task.id
                        	FROM
                        		children_of_task
                        	JOIN parents_of_allowed_tasks
                        		ON children_of_task.id = parents_of_allowed_tasks.id
                        	
                        )
                             
                        &finalSelect
                        """;
    }

    private String getFinalSelect(boolean isCount) {
        if (isCount) {
            return """               
                        SELECT
                        	CAST(COUNT(*) AS INT)
                        FROM
                        	allowed_tasks
                    """;
        }
        return """
                SELECT
                        	*
                        FROM
                        	project_tasks
                        WHERE
                        	project_tasks.id IN(
                        	SELECT
                        		id
                        	FROM
                        		allowed_tasks
                        	)
                        ORDER BY
                        	level_order
                """;
    }

}
