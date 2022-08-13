# stitch-demo

This is a client integration app to stitch.money

For building and running the application you need:

- [JDK 1.8] and above (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven 3](https://maven.apache.org)

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `com.stitch.payments.demo.DemoApplication` class from your IDE.

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

```shell
mvn spring-boot:run
```
This sample covers the following features:
- Generate a client token
- Generate a user token using authorization flow
- Fetch financial data
- Use the refresh token to retrieve a new token
- Use InstantPay to:
  - Create a payment initiation request
  - Handle payment request callback
  - Subscribe for payments completion webhook notifications
  - Receive *signed* webhooks on payment completion
  - Retrieve payment request status
- Use LinkPay to:
  - Create payment authorization request
  - Present user with authorization flow
  - Retrieve user tokens
  - Initiate payment
  - Handle user-interaction for accounts that require MFA
  - Subscribe to paymentinitiation signed webhook
  - Receive and validate signed HMAC webhook notification
