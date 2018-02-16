# About
The gateway HelloWorld assertion repository is an example repository for a modular assertion. It describes how the CA API Gateway can be extended. 

# Features
* Simple assertion to leave the response text as "Hello World!".
* Introducing new scheme/protocol (i.e. HelloWorld.SCHEME) and hence user can define listen port using it.
  * Supports text messages.
  * Treats every line of input text as separate message and pushes to hardwired to service policy for further processing.
  * Processed response will be delivered as line of text.

# Build
In order to build the modular assertion run `gradle build`.
 
This will compile, test, and create the aar file. It will be available in build/libs

# Adding Libraries
In order to build modular assertions some gateway jars are required they need to be put into a `lib` directory. The required jars are:
* layer7-common-9.2.00.jar
* layer7-gateway-common-9.2.00.jar
* layer7-gateway-server-9.2.00.jar
  * This is `Gateway.jar`.
* layer7-policy-9.2.00.jar
* layer7-utility-9.2.00.jar
* layer7-gateway-console-9.2.00.jar
  * This is the `Manager.jar` from the policy manager.

Few more from the third party libraries.
* spring-context-3.0.7.RELEASE.jar
* javax.inject-1.jar

# Run
Docker version of gateway greatly helps us to deploy assertions / RESTMAN bundles quickly. Please follow the steps below to run the gateway along with it.
1) Open Shell or Command Prompt and navigate to the directory where this repository is cloned.
2) Ensure the docker environment is properly setup. 
3) Provide the gateway license at `docker/license.xml` while running the container.
4) Execute the below docker-compose command to run the CA API Gateway container.
   ```
     docker-compose up
   ```
   * Provided `docker-compose.yml` ensures pulling the latest CA API Gateway image from the Docker Hub public repository and deploys the Helloworld assertions from the libs directory.
   * In addition, it publishes one listen port and two services using `docker/helloworld.req.bundle` RESTMAN bundle.
     * Hello World [/Hi] Service: Returns response text as "Hello World!".
     * Pre-configured listen port 8081 with HelloWorld.SCHEME scheme/protocol.
     * Hello World Scheme Default Handler Service: Hard wired to the listen port 8081. It looks for [Hello .*] pattern in the body of an http request and replaces it with "Hello gateway".
7) Wait for the gateway container fully up and ready to receive messages.


# Test the Hello World service
```
curl http://localhost:8080/hi
```
Expected response: Hello World!

# Test the listen port with HelloWorld.SCHEME

Prepare your input as multiple lines
```
echo Hi > input.txt
echo Hello World >> input.txt
```

Send the message from the file to listen port 8081
```
curl --data-binary @input.txt http://localhost:8081
```
Expected response is as follows:
```
You said: Hi
You said: Hello gateway
```

# How to create new listen port with HelloWorld.SCHEME
1) Connect to the running gateway via Policy Manager. You can find the admin credentials from the docker-compose file. 
2) Navigate to Tasks -> Transports -> Manage Listen Ports to open the Manage Listen Ports window.
3) Click on Create button to define new listen port with HelloWorld.SCHEME scheme/protocol.
4) In the Advanced tab of Listen Port Properties dialog, choose the service to process the incoming messages.
5) Click on OK button.


## How You Can Contribute
Contributions are welcome and much appreciated. To learn more, see the [Contribution Guidelines][contributing].

## License

Copyright (c) 2017 CA. All rights reserved.

This software may be modified and distributed under the terms
of the MIT license. See the [LICENSE][license-link] file for details.


 [license-link]: /LICENSE
 [contributing]: /CONTRIBUTING.md