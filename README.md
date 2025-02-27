# IoT Enhanced Business Processes supported by BPMN and Microservices. Infrastructure

This repositry contains a software infrastructure to define an IoT-enhanced business proccess (BP). These BPs are defined in the IoT domain and multiple services, machines, and things can take the role and responsibility of performing some of the process tasks. An example using this infrastructure is available in the following Github repository: [iot-enhanced-business-process-example](https://github.com/pvalderas/iot-enhanced-business-process-example).

It has been implemented by using:

* [Spring Boot](https://spring.io/projects/spring-boot)
* [Netflix infrastructure](https://github.com/Netflix)
* [Camunda Engine](https://github.com/camunda/camunda-bpm-spring-boot-starter)
* [BPMN.io](https://github.com/bpmn-io)
* [RabbitMQ](https://www.rabbitmq.com/)

# About

This is the result of a reserach work leaded by Pedro Valderas at the PROS Research Center, Universitat Politècnica de València, Spain.

This work presents a modelling approach based on BPMN that reuses the concepts introduced by this language in order to model IoT-enhanced BPs. This modelling approach allows specifying IoT devices and both push and pull interactions between the process and Iot Devices, without modifying the BPMN metamodel. 

This modelling approach is suppoted by a microservice architecture aimed at facilitating the integration of business processes with the physical world that provides high flexibility to support multiples IoT device technologies, and facilitates evolution and maintenance.

# The proposed architecture

The microservice architecture to support IoT-enhanced business proccess is shown below.

![architecture](./architecture.png "Proposed Architecture")

The architectural elements that support our proposal are depicted in red. Thery are the following:

* Service Registry: this microservice is in charge of maintaining the list of business microservice that there are in the system. For each microservice, this registry stores its invocation data. We used Netflix’s Eureka , which allows registering different instances of microservices. This registry also provides a REST API in order to interact with it through the HTTP protocol, which allows the Action Performer to access microservice invocation data easily. 

* BP Controller: this microservice is endowed with: (1) an adapted BPMN.io modeller to create IoT-enhanced business processes, and (2) a Camunda BPMN engine that is in charge of controlling the activity flow of the processes. This engine does not directly interact with business microservices. Instead it sends execution requests to the Action Performer microservice presented below. To do so, the only restriction that we must consider is that the service tasks associated to the lanes that represent actors should be bind to the API provided by the Action Performer microservice. 

* Action Performer: this microservice plays the role of middleware among the BP Controller, the Service Registry, and the business microservices. It publishes an API to which BPMN service tasks must be bind in order to execute an action. When a service tasks is executed by the BPMN engine of the BP Controller, this engine sends an execution request to the Action Performer, which interact with the Service Registry in order to know the invocation data of the required business microservice. Then, it calls the corresponding operation accessing the business microservice.

* Context Monitor: this microservice is in charge of registering to the event-based bus in order to access the context changes published by business microservices. When a context change happens, this microservice registers it into an OWL-based context ontology which is continously analyzed in order to generate high level events. These events are injected into the BP Controller. 

* Event bus: it supports the asynchronous communication required by the infrastructure based on a publish/subscribe patter. The RabbitMQ  queue-based message broker is currently supported. 

* IoT Devices: they are the microservices in charge of controlling IoT devices through REST calls.

In order to create the BP Controller, the Action Performer, the Context Manager and the IoT Devices microservices we provide the following tool-support.


# Creating a Business Process Controller

To create a BP Controller you can use Gradle to build the corresponding project in this repository and include it as a dependency of a Spring Boot Application. Then, you just need to annotate the main class with the ```@BPController``` as presented bellow. Note that the ```@SpringBootApplication``` annotation must be configured to find beans in the ```es.upv.pros.pvalderas.bpcontroller.server``` package.

```java
@BPController
@SpringBootApplication(scanBasePackages = {"es.upv.pros.pvalderas.bpcontroller.server"})
public class BPControllerMain {
  public static void main(String[] args) {
    SpringApplication.run(BPControllerMain.class, args);
  }  
}
```
Next, you must create an application.yml file, to name the microservice 'BPController' (mandatory name), indicate its HTTP port, define the the urls of both the Action Performed and the Context Monitor, and specify the configuration regarding to the service registry Eureka and the event bus.

```yml
spring:
  application:
    name: BPController
    
server:
  port: 8081
  
actionPerformer: 
  serviceUrl: http://localhost:8082/iot/microservice
  name: ActionPerformer
  
contextMonitor:
    serviceUrl: http://localhost:8083
    conditionPath: /context/query
  
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:2222/eureka
    registerWithEureka: true
    fetchRegistry: true

eventBus:
  type: rabbitmq
  host: localhost
```
 
# Creating an Action Performer

To create an Action Performer you can use Gradle to build the corresponding project in this repository and include it as a dependency of a Spring Boot Application. Then, you just need to annotate the main class with the ```@ActionPerformer``` as presented bellow. Note that the ```@SpringBootApplication``` annotation must be configured to find beans in the ```es.upv.pros.pvalderas.actionperformer.server``` package.

```java
@ActionPerformer
@SpringBootApplication(scanBasePackages = {"es.upv.pros.pvalderas.actionperformer.server"})
public class ActionPerformerMain {
  public static void main(String[] args) {
    SpringApplication.run(ActionPerformerMain.class, args);
  }   
}
```
Next, you must create an application.yml file to name the microservice 'ActionPerformer' (mandatory name), indicate its HTTP port, and define the configuration of the service registry Eureka and the event bus.

```yml
spring:
  application:
    name: ActionPerformer
    
server:
  port: 8082
  
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:2222/eureka
    registerWithEureka: true
    fetchRegistry: true

eventBus:
  type: rabbitmq
  host: localhost
```

# Creating an Context Monitor

To create a Context Monitor you can use Gradle to build the corresponding project in this repository and include it as a dependency of a Spring Boot Application. Then, you just need to annotate the main class with the ```@ContextMonitor``` as presented bellow. Note that the ```@SpringBootApplication``` annotation must be configured to find beans in the ```es.upv.pros.pvalderas.contextmonitor.ontology``` package.

```java
@ContextMonitor(contextOntology=PurchaseContextOntology.class)
@SpringBootApplication(scanBasePackages = {"es.upv.pros.pvalderas.actionperformer.server"})
public class ContextMonitorMain {
  public static void main(String[] args) {
    SpringApplication.run(ActionPerformerMain.class, args);
  }   
}
```
Next, you must create an application.yml file to name the microservice 'ActionPerformer' (mandatory name), indicate its HTTP port, and define the configuration of the service registry Eureka and the event bus.

```yml
spring:
  application:
    name: ContextMonitor
    
server:
  port: 8083
  
bpController:
    serviceUrl: http://localhost:8081
    messagePath: /process/message

eventBus:
  type: rabbitmq
  host: localhost
 
contextOntology:
  name: ContextOntology
  path: models

# Creating a microservice to support an IoT device

To create an IoT microservice that interact with the Action Performed through synchronous REST invocations you can use Gradle to build the corresponding project in this repository and include it as a dependency of a Spring Boot Application. Then, you just need to annotate the main class with the ```@IoTDevice``` as presented bellow. This annotation must be configured with the class object of the microservice HTTP controller. Note also that the ```@SpringBootApplication``` annotation must be configured to find beans in the ```es.upv.pros.pvalderas.microservice.syncronous``` package as well as the package in which the HTTP controller of the microservice is implemented (```es.upv.pros.pvalderas.truckcontainer``` in the example below). In addition, the ```@EnableDiscoveryClient``` annotation is used to register the microservice in Eureka.

```java
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"es.upv.pros.pvalderas.truckcontainer","es.upv.pros.pvalderas.microservice.iotdevice"})
@IoTDevice(serviceAPIClass=TruckContainerSensorHTTPController.class)
public class TruckContainerSensor {
  public static void main(String[] args) {
    SpringApplication.run(TruckContainerSensor.class, args);
  } 
}
```
Next, you must create an application.yml file, indicating the name of the microservice (which is shown in the BPMN editor provided by the BPController), indicate its HTTP port, and define the configuration of the service registry Eureka.

```yml
spring:
  application:
    name: TruckContainer
    
server:
  port: 8091
    
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:2222/eureka
  instance:
    metadataMap:
      connectionType: synchronous
```

# Using the infrastructure to create and execute an IoT-enhanced business proccess

In [iot-enhanced-business-process-example](https://github.com/pvalderas/iot-enhanced-business-process-example) you can find the implementation of a case study based on the process of purchase orders.

# Acknowledgement

Grant MCIN/AEI/10.13039/501100011033 funded by: 

<img src="./mcin.png" alt="Ministeria de Cienca e innovación" width="300px"> <img src="./aei.png" alt="Agencia Estatal de Investigación" width="100px"> 
