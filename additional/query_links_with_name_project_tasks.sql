select 
	links.*, 
	project_tasks.name, 
	tasks1.name name_linked 
from links 
join project_tasks on links.project_task = project_tasks.id
join project_tasks tasks1 on links.linked_project_task = tasks1.id