package com.pmvaadin.projecttasks.services.role.level.queries;

import com.pmvaadin.projectstructure.Filter;

public class QueryBuilderForOnlyInListAccess {

    private final String nameOfAllowedTasks = "&allowedTasks";
    private final String nameOfFinalSelect = "&finalSelect";
    private String condition;

    public String getQueryTextForUpperLevel(boolean isCount, String ids,
                                            Filter filter) {

        String queryText;
        queryText = getQueryForUpperLevelWithoutFullHierarchy();

        var fillingOfFinalSelect = getFinalSelect(isCount);
        queryText = queryText.replace(nameOfFinalSelect, fillingOfFinalSelect);
        queryText = applyFilters(queryText, filter);
        queryText = queryText.replace(nameOfAllowedTasks, ids);

        return queryText;

    }

    public String getQueryTextForParent(boolean isCount, String ids, String parentId,
                                        Filter filter) {

        var queryText = getQueryTextForChildrenOfParent();
        var fillingOfFinalSelect = getFinalSelect(isCount);
        queryText = queryText.replace(nameOfFinalSelect, fillingOfFinalSelect);
        var parent = "'{" + parentId + "}'";
        queryText = queryText.replace("&parentId", parent);
        queryText = applyFilters(queryText, filter);
        queryText = queryText.replace(nameOfAllowedTasks, ids);

        return queryText;

    }

    public String getQueryTextForParentFullRights(boolean isCount, Object parentId, Filter filter) {

        fillConditionByParentId(parentId);

        return getQueryTextFullRightsWithExcludedTasks(isCount, filter);
    }

    public String getQueryTextForParentFullRights(boolean isCount, Object parentId, Filter filter, String excludedIds) {

        fillConditionByParentId(parentId);

        if (!excludedIds.isEmpty()) {
            condition = condition + "\n" +
                    "AND NOT (id = ANY(&excludedIds))";
            condition = condition.replace("&excludedIds", excludedIds);
        }
        return getQueryTextFullRightsWithExcludedTasks(isCount, filter);
    }

    private void fillConditionByParentId(Object parentId) {
        if (parentId == null)
            this.condition = "parent_id ISNULL";
        else
            this.condition = "parent_id = " + parentId;
    }

    private String getQueryTextFullRightsWithExcludedTasks(boolean isCount, Filter filter) {

        String queryText;
        queryText = getQueryTextForChildrenOfParentUnified();

        var mainQuery = getMainQuery();
        mainQuery = mainQuery.replace("&condition", condition);

        queryText = queryText.replace("&mainQuery", mainQuery);
        var fillingOfFinalSelect = getFinalSelect(isCount);
        queryText = queryText.replace(nameOfFinalSelect, fillingOfFinalSelect);
        //TODO change applyFilter so excluded tasks make effect on getting parents of project tasks
        queryText = applyFilters(queryText, filter);

        return queryText;

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
                                
                allowed_tasks_from_rights AS (
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
                	
                ),
                
                &tasksFilterTable
                
                allowed_tasks AS (
                SELECT
                	allowed_tasks_from_rights.id
                FROM
                	allowed_tasks_from_rights
                &filterJoin
                )
                                
                &finalSelect
                """;

    }

    private String getQueryForUpperLevelWithoutFullHierarchy() {
        return
                """
                                        
                        WITH RECURSIVE checking_allowed_tasks_by_upper_hierarchy AS (
                                        
                            SELECT
                                id,
                                id id_iter,
                                FALSE remove_id,
                                ARRAY[id] path -- protection of cycle
                            FROM project_tasks
                            WHERE id = ANY(&allowedTasks)
                                        
                            UNION ALL
                            
                            SELECT
                                a.id,
                                p.parent_id,
                                p.parent_id = ANY(&allowedTasks) OR p.parent_id = ANY(a.path),
                                a.path || p.parent_id
                            FROM checking_allowed_tasks_by_upper_hierarchy a
                            JOIN project_tasks p
                                ON a.id_iter = p.id
                                AND NOT a.remove_id
                            
                        ),
                                        
                        removing_tasks AS (
                            SELECT DISTINCT
                                id
                            FROM
                                checking_allowed_tasks_by_upper_hierarchy
                            WHERE
                                remove_id
                        ),
                                        
                        allowed_tasks_from_rights AS (
                            SELECT DISTINCT
                                id
                            FROM
                                checking_allowed_tasks_by_upper_hierarchy
                            WHERE
                                id NOT IN(SELECT
                                            id
                                         FROM
                                            removing_tasks)
                        ),
                             
                        &tasksFilterTable
                             
                        allowed_tasks AS (
                            SELECT
                                allowed_tasks_from_rights.id
                            FROM
                                allowed_tasks_from_rights
                            &filterJoin
                            )
                             
                        &finalSelect
                        """;
    }

    private String getQueryTextForChildrenOfParentUnified() {

        return """
                WITH allowed_tasks_from_rights AS (
                    &mainQuery
                ),
                
                &tasksFilterTable
                
                allowed_tasks AS (
                SELECT
                	allowed_tasks_from_rights.id
                FROM
                	allowed_tasks_from_rights
                &filterJoin
                )
                                
                &finalSelect
                """;

    }

    private String getMainQuery() {
        return """
                SELECT
                	id
                FROM
                	project_tasks
                WHERE
                	&condition
                	--parent_id ISNULL
                	--AND NOT id = ANY()
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

    private String applyFilters(String queryText, Filter filter) {

        var taskFilterPart = "&tasksFilterTable";
        var joinFilterPart = "&filterJoin";
        var filterByName = filter.getFilterText();
        var filterByProject = filter.isShowOnlyProjects();
        var addFilterByName = filterByName != null && !filterByName.trim().isEmpty();
        if (!addFilterByName && !filterByProject) {
            queryText = queryText.replace(taskFilterPart, "");
            queryText = queryText.replace(joinFilterPart, "");
            return queryText;
        }
        var condition = "";
        if (addFilterByName) {
            condition = "name ILIKE '%" + filterByName + "%'";
        }
        if (filterByProject) {
            if (addFilterByName) {
                condition = condition + "\n" + "    AND is_project";
            } else {
                condition = "is_project";
            }
        }

        var conditionPart = "&condition";
        var tasksFilter = getTasksFilterTableQueryText();
        tasksFilter = tasksFilter.replace(conditionPart, condition);

        queryText = queryText.replace(taskFilterPart, tasksFilter);
        queryText = queryText.replace(joinFilterPart, getTextFilterJoinText());

        return queryText;

    }

    private String getTasksFilterTableQueryText() {
        return """
                tasks_filter AS (
                	SELECT
                		array_agg(id)
                	FROM
                		project_tasks
                	WHERE
                		--name ILIKE '%xc%'
                		&condition
                ),
                                
                tasks_filter_with_parents AS (
                SELECT
                	parents.id id
                FROM
                	get_parents_in_depth(
                	(
                		SELECT
                			*
                		FROM
                			tasks_filter
                	)
                ) parents
                ),
                """;
    }

    private String getTextFilterJoinText() {
        return """
                JOIN tasks_filter_with_parents
                    ON allowed_tasks_from_rights.id = tasks_filter_with_parents.id
                """;
    }

}
