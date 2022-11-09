Project management
=

This is the Gradle Spring+Vaadin project, designed for project management.

The project uses Spring Security, so the following data is used to authenticate the user: phone number: 000, password: ADMIN 

The user can view three sections:

Section Projects:

<img src="images/Projects.png" alt="Projects" width="100%"/>

In this section, the following actions are available with project tasks:
+ Addition.
+ Deletion.
+ Moving. You can move tasks within the tree using buttons and drag and drop with the mouse
+ Double-clicking opens the task editing form (Task data can only be changed by using the edit form)

<img src="images/Project_task.png" alt="Project task" width="100%"/>

+ + The "Main" tab contains the main data of the task

<img src="images/Predecessors.png" alt="Predecessors" width="100%"/>

+ + On the "Predecessors" tab, you can set task predecessors.
(Not all verifications have been done for the possibility of adding predecessors and verifications the possibility of moving tasks within the tree, taking into account predecessors).
You can add a predecessor by clicking on the Add button,
in the Project tasks column click on the icon <img src="Images/selection_icon.png" alt="selection_icon" width="25"/>.
In the selection form that opens, select the required task, either by clicking on the Select button or by double-clicking the mouse.
In the second column, select Link type.

In the Calendars section, you can add project task calendars, but they are not used yet.

In the Users section, you can create new users and edit current ones.
