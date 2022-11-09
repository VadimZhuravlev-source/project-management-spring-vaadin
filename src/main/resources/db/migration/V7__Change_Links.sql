ALTER TABLE project_tasks
ADD links_check_sum INT NOT NULL DEFAULT 0;

ALTER TABLE links
DROP CONSTRAINT links_project_task_fkey;

ALTER TABLE links
ADD CONSTRAINT links_project_task_fkey
FOREIGN KEY (project_task)
REFERENCES project_tasks(id)
ON DELETE CASCADE;

ALTER TABLE links
DROP CONSTRAINT links_linked_project_task_fkey;

ALTER TABLE links
ADD CONSTRAINT links_linked_project_task_fkey
FOREIGN KEY (linked_project_task)
REFERENCES project_tasks(id)
ON DELETE CASCADE;