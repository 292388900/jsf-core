# jsf-core

## What is JSF-CORE?

        JSF-core is jsf server console which provide management for JSF client. The following functions 
 are provided, such as:registration management,schedule task. In the future, we will also provide more 
 extensive management functions.

## What does JSF-CORE do?

* Registry center
    *  service registry
    *  service subscribe
    *  service discover
    *  service push
    *  service configration push
    *  service health monitor

* worker is schedule task
    * monitor the aviable status of registry
    * Delete expired data in mysql
    * count number of tcp 
    * count number of provider
    * count number of consumer
    
## Database
We use mysql database. We should install mysql before. There are two databases: saf21 and saf_registry, which sql files were In the following directory: ${jsf-core}/jsf-common/sql. 

## dependencies
    	Zookeeper 3.0+
		Mysql 5.5+
		Maven 3.1.x
		JDK1.8.0+
		JSF SDK(https://github.com/tigcode/jsf-sdk)
