# mychat-resource-server

[application online demo](https://www.wewehappy.com)

The Chat App Resource Server is a component of the Chat App project that serves as the backend for managing and providing resources for the chat application. It is built with Spring Boot 3.

## Features

- OAuth2 authentication using Google identity
- Custom JWT authentication
- Management of chat-related resources (messages, users, etc.)

## Prerequisites

- Java Development Kit (JDK) 17 or higher installed on your system.
- Apache Maven for building and running the project.
- Access to Google identity provider for OAuth2 authentication.

## Setup and Configuration

1. Clone this repository to your local machine:

   ```shell
   git clone https://github.com/Marcwe25/mychat-resource-server.git
   ```
2. Navigate to the project directory:

   ```shell
   cd mychat-resource-server
   ```
3. Configure the application properties:

	Open the application.properties file located in the src/main/resources directory. Provide the necessary configuration values, such as the database connection details, OAuth2 client credentials, and any other required settings.

4. Build and run the application:

	```
	mvn spring-boot:run
	```
	The resource server will start running and listenning to http://localhost:8080
