# Prioritize

image::http://www.prioritize-iot.de/logo.png[]


**NEW: REST API docs now available (First version):**
http://www.prioritize-iot.de/apiDoc

If you have contributed code which has already been merged/accepted you can watch the build state here:
<p></p>
http://jenkins.prioauth.com:8080/
 


## What it is:

Prioritize is an open source JakartaEE-Framework to accomplish basically the following tasks:

* Create and manage **companies** and different **departments** as basic structures.
* Create and manage **users** and **roles** within that structures
* Create and manage **documents** and **devices** (IOT) over MQTT
* Create and manage **skills** for users and devices 
* Create and manage **projects**, **tasks** and project **blackboards**
* **calendar** and scheduler to use with these objects
* Store and display sensor data from different devices (e.G. temperature from a smartphone) via MQTT and REST interface.
* Assign tasks from projects to users **and** devices!
* 

It can be installed as a JakartaEE-Application. 
It comes with a ready to use REST-API. You can perform different tasks by calling the REST-API Endpoint and provide the correct credentials (Department-Token, User-Token).

IMPORTANT: A local or remote Database, Application Server (e.G. Wildfly) and a persistence unit must be installed/configured.
See the files persistence.xml as an example.

 

## Getting started (Quickstart)

### Prerequisites

#### Application Server
Due to the fact that prioritize is a JakartaEE Application you have to setup your own local or remote Application server to host JavaEE Applications. I recomment the wildfly application server which is available here: https://wildfly.org/

#### Database
Make sure your application server is configured with a valid DBMS connection, connection pool and persisitence unit to handle JPA requests.
I recommend to use MariaDB or MySQL.
 


### deploy and login
You can build Prioritize with the following maven command:

    mvn clean install

You can use your favorite IDE to accomplish this. After the build is finished you can simply deploy that application archive on the application server of your choice (testet on wildfly).   

Now you can issue REST calls to work with Prioritize. 

default user is "admin" with default password "admin".<p></p>

**IMPORTANT: Make sure to change the password after installation! At the moment the password

<p></p>

Also don't forget to edit persistence.xml to hold the datasource you have created on your Application Server.
By default the following datasource configuration is used assuming that there is a DataSource called "MySqlDS":


```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.1" xmlns="http://xmlns.jcp.org/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd">
	<persistence-unit name="Prioritize">
	 <jta-data-source>java:jboss/datasources/MySqlDS</jta-data-source>
	 <properties>
      <!-- Properties for Hibernate -->
       <property name="hibernate.dialect" value="org.hibernate.dialect.MySQLDialect" />
         <property name="hibernate.hbm2ddl.auto" value="none" />
         <property name="hibernate.show_sql" value="false" />
      </properties>
	</persistence-unit> 
</persistence>
```



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

You can completely use the REST interface of prioritize to write your own client (e.G. by using VuJS, angular or any other technology which is capable of communicating with the REST backend. 


### Files for deployment



### Configurtation
At the moment there is one custom configuration file called **"config.ini**" located here:

    ejbModule/META-INF/resources/config.ini.

It is very good documented and should explain all relevant details. 


## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[Apache] (http://www.apache.org/licenses/LICENSE-2.0)

  PLEASE NOTE:
  
  The Program includes all or portions of the following software 
  which was obtained under the terms and conditions of the 
  Eclipse Public Licence 1.0  (https://www.eclipse.org/legal/epl-v10.html):
  
  Eclipse Paho Java Client https://www.eclipse.org/paho/clients/java/
