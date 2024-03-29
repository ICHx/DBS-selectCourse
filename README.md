Project DBS-selectCourse
--
### Introduction
This is my implementation of an imaginary course selection system.  Backed by a relational database, hosted by SparkJava web framework and interfaced with Vue.js client.
### Hosted Instance
[Hosted site](https://second-pursuit-310310.df.r.appspot.com/)<br>
The SparkJava application is hosted on Google App Engine Standard Environment with Java11.
Since GAE only allows read-only access to SQLite datafile, ~~exceptions are caught.  But can be ignored, data query and operations are still simulated.~~ db is copied to /tmp, changes persist as long as the app instance is running.

![image](https://user-images.githubusercontent.com/11556527/129689687-51d5aba5-e55a-4841-97a7-ddca8cf84266.png)
*Available Course List*

![preview](docs/preview.png)

*Selected Course List*

### Todo
[Todo list](TODO.md)


<hr>
### Project Requirements

DB Select course System
-	Vaadin/SparkJava
-	sql2o or Mybatis


#### Function
-	list selectable courses
-	select courses (cart)
-	edit-selected course (cart edit)

#### Constrains
-	21 max credit
-	Dependency Courses
-	Time conflict resolution



## Build

### Directory Structure
- /docs - documentation
- /src/main/java - java source files
- /src/main/resources - html/js source files
- /target - compiled executables
- /db - default database location

### Compilation
1. MVN clean
2. MVN compile
3. MVN package
4. The executable jar is in /target

> By default operations are reverted.  To make permanent change, change /src/main/java/Main.java DEBUG constant to 0. 
> 


## Progress Screenshots
![image](https://user-images.githubusercontent.com/11556527/111413472-4b2b6780-8719-11eb-9b53-fddde8011da6.png)
\
Before Bootstrap


![image](https://user-images.githubusercontent.com/11556527/111413680-aeb59500-8719-11eb-97c9-9c3e7cfd6db2.png)
\
After Bootstrap
