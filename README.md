JSF- An RPC library and framework
===================================

# What is JSF
  
JSF is an RPC framework developed by the **TIG**(Technical Infrastructure Group).
   
It is now supporting thouthands of applications, providing over 200 billions of RPC calls everyday. During the China's big shopping festivals like Double 11 and 618, the total number of JSF-based RPC calls can reach 400 billion.
   
## What is JSF-core
 
 Basically, JSF-core is the backend part of the JSF framework. It provides many underlying services like service registration and task scheduling.  Hopefully, more and more features would be added in as time goes on.

## Features of JSF-core

* Registration Center
    *  service registering
    *  service subscribing
    *  service discovering
    *  service pushing
    *  service configuration pushing
    *  service health monitoring

*  Task Scheduling
    * monitoring the availability status of Registration Center
    * removal of expired data
    * counting TCP connection, providers and consumers

# Dependencies  

* Zookeeper 3.0+

* Mysql 5.5+

* Maven 3.1.x

* JDK 1.8+

* [JSF SDK](https://github.com/tigcode/jsf-sdk)

# More details

## Database
Currently, MySQL was chosen as the database server. There are two databases saf21 and saf_registry on the server, with SQL scripts located in the directory ${jsf-core}/jsf-common/sql. 
