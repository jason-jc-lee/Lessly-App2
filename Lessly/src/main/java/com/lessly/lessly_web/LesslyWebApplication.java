package com.lessly.lessly_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This java file is where the Spring Boot application can begin to configure the app (also the entry point).
 * The @SpringBootApplication is what allows it to do so, allowing for access to components and services.
 * In addition, this file and Spring Boot sets up the Tomcat web server (it is in-built in Spring Boot). This web server listens for requests when someone enters localhost:8080 and routes that to the Java code.
 */

@SpringBootApplication
public class LesslyWebApplication {
// starts up the web server and initializes all Spring components
	public static void main(String[] args) {
		SpringApplication.run(LesslyWebApplication.class, args);
	}

}
