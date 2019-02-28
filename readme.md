# Spring-Boot Based CQRS demo application

This sample uses Spring cloud stream with Kafka binder. Kafka stream API is
also used to help maintain a queryable snapshot of an aggregate on the command-side module.

## Step to run this demo application:
 
 * [Download](https://kafka.apache.org/downloads) kafka
 * extract the downloaded file, and navigate into the kafka home directory
 *  run this command to bring up ZooKeeper
 ``` shell
 bin/zookeeper-server-start.sh config/zookeeper.properties 
 ```
 * run this command to bring up Kafka
 ```shell 
 bin/kafka-server-start.sh config/server.properties
 ```
 * run all applications