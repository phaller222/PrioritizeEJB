# Prioritize

![Prioritize logo](http://www.prioritize-iot.de/logo.png)

##IMPORTANT: This project consists of three projects:

[PrioritizeEJB](https://github.com/phaller222/PrioritizeEJB.git) - model and controller classes for the backend

[PrioritizeWeb](https://github.com/phaller222/PrioritizeWeb.git) - mainly xhtml GUI files

[Prioritize](https://github.com/phaller222/Prioritize) - main project which builds an .ear archive from the other two.

If you want to install and run Prioritize or contribute please make sure you have checked out all projects above from git. 



## What it is:

Prioritize is an open source  javaEE-Framework to accomplish basically the following tasks:

* Create and manage **companies** and different **departments** as basic structures.
* Create and manage **users** and **roles** within that structures
* Create and manage **documents** and **devices** (IOT) over MQTT
* Create and manage **skills** for users and devices 
* Create and manage **projects**, **tasks** and project **blackboards**
* **calendar** and scheduler to use with these objects
* Store and display sensor data from different devices (e.G. temperature from a smartphone) via MQTT and REST interface.
* Assign tasks from projects to users **and** devices!
* 

It can be installed as a J2EE ear-Application and comes with a ready to use administration GUI to create and manage the different kinds of objects needed (Users, tasks, companies...).
It also comes with a ready to use REST-API. You can perform different tasks by calling the REST-API Endpoint and provide the correct credentials (Department-Token, User-Token).


## Motivation

When i began working on prioritize about 4 years ago i just wanted to develop my own simple project management system because the features of those available at that time did not match my needs. Additionally i finally wanted to finish my first bigger J2EE project. Before that time i started twice developing a kind of workflow system. But prior to J2EE 5 as you might know there was the "XML configuration hell" and you had to write so much boilerplate code to get things running. So i stopped that project twice until 4 years ago :-)

The first positive results with the newer JavaEE-Versions which just needed a few Annotations to be added increased my motivation so that until now there was a continous development on prioritize by me. 
Then the internet of things hype came up and i stumbled upon MQTT, a very lightweight, easy to use protocol. I wrote a connector for prioritize and connected my first smartphone to prioritize.
That was the time i decided thast prioritize must not die! But for me only it prioritize became too big with so many feature to maintain and complete so i finally decided to release the core of the framework as open source.

 
 

## Getting started (Quickstart)

### Prerequisites

#### Application Server
Due to the fact that prioritize is a JavaEE Application you have to setup your own local or remote Application server to host JavaEE Applications. I recomment the wildfly application server which is available here: https://wildfly.org/

#### Database
Make sure your application server is configured with a valid DBMS connection, connection pool and persisitence unit to handle JPA requests.
I recommend to use MariaDB or MySQL.

 


### deploy and login
You can build all three necessary projects of Prioritize (PrioritizeEJB, PrioritizeWeb and Prioritize), exactly in that order with the following maven command:

    mvn clean install

You can use your favorite IDE to accomplish this. After the build is finished you should see an .ear-File in the Prioritize project's target directory (At the writing of theese line it is *"Prioritize-0.0.1-SNAPSHOT.ear"*. It's size should be about 20 - 25 MBytes. You can then simply deploy that application archive on the application server of your choice (testet on wildfly).   

Now you can access the main Prioritize admin pages by entering the following URL:
http://localhost:8080/PrioritizeWeb/admin/index.xhtml
 
default user is "admin" with default password "prioritizeadmin". 


## Getting started 

### Technologie and structure
The heart of the prioritize project is the **PrioritizeEJB** project. Here all backend functionality and REST endpoints are coded. 
Basically there are 3 main packages, which are as follows: 

`de.hallerweb.enterprise.prioritize.model` - The "model" classes like User, Department, Document... 
											with getters and setters. 
`de.hallerweb.enterprise.prioritize.controller` - Classes which operate on the model, use JPA and perform queries.

`de.hallerweb.enterprise.prioritize.view` - ViewBeans which act as backend for primefaces xhtml-pages 
								     and access the controller. 

For almost every model-class like **Calendar** or **Skill** their is a correspondiong controller class in the controller package. In this case **CalendarController** and **SkillController**. Some model classes like **User** and **Role** for example share common Controller and View classes because their usecase is very related to another. So User and Role instances are both managed by the **UserRoleController** class for example. 
Prioritize defines the following main objects in the model with their respective responsibility. If you look at theese main model objects you should get a pretty good basic overview of what prioritize is capable of. :

**Company** - 				Defines a company. Prioritize can host more than one company.

**Department** - 			Defines a department (e.G. sales) within a company.	

**User** -					As it states, a User of the system usually equipped with credentials to log in.

**Role** - 					A role a user can be part of (eG. admin role).

**PermissionRecord** - 		Important class to realize the prioritize permissionm model.

**Resource** - 				A resource, or device can represents an IoT-device and communicate with the outside.	

**Document** - 				A class representing a concrete document (e.G. a PowerPoint file uploaded by a user).

**Skill** - 					Users can have skills (e.G. selling). But skills can also be assigned to resources / devices!

**Calendar** - 				As it states a class to represent a calendar holding illness, vacation and other meeting data. 

**Project** - 				Main class to represent projects in prioritize

**Blackboard** -				Implements the concept of a blackboard. All tasks of a project can be picked by any user. 

**ActionBoard** - 			Ment to be a kind of message board with user defined data beeing displayed.	

**Task** - 					Represents a task of a project.

**TimeTracker**				An implementation of a simple timetracker functionality.

**ProjectGoal**				Class holding information about project goals, which can be determined automatically.
...

If you want to take a deeper look of which actions are performed on or with theese objects open their respective controller classes (e.G. ActionBoardController, CalendarController... 

Besides the PrioritizeEJB-project there is a second project, **PrioritizeWeb**, which mainly holds the primefaces GUI webpages for prioritize. Within that project you can see a directory structure with xhtml files almost identical to the package structure in the PrioritizeEJB project. 
As an alternative, you can completely use the REST interface of prioritize, if finished some day ;-) to write your own client (e.G. by using VuJS, angular or any other technology which is capable of communicating with the REST backend. 

The last project, **Prioritize**, just uses the other two projects and generates an .ear-File for deployment out of them.


### Files for deployment

The only file which is needed for deplyment is the .ear-file built by the main project "**Prioritize**":

"Prioritize-0.0.1-SNAPSHOT.ear" for version 0.0.1 at the writing of theese lines. Just take the file from the target folder and deploy it on a suitable application server. You should then be able to star and use prioritize.

### Configurtation
At the moment there is one custom configuration file called **"config.ini**" located here:

    ejbModule/META-INF/resources/config.ini.

It is very good documented and should explain all relevant details. 


## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[Apache] (http://www.apache.org/licenses/LICENSE-2.0)
