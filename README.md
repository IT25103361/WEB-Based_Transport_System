# NEXT GO — Integrated Transport & Services Platform

**One platform for everyday mobility, deliveries and emergency service requests.**

NEXT GO is a web-based application that brings six transport and service modules into a single platform: Ride Management, Courier Delivery, School Student Transport, Food & Grocery, Vehicle Rental, and Emergency Services.

Developed as a collaborative Software Engineering project at the **Sri Lanka Institute of Information Technology (SLIIT)**, the system applies object-oriented programming, layered architecture, database persistence and selected design patterns to practical service workflows.

## Project Overview

Managing transport and delivery services through separate systems can make booking, tracking and record management difficult. NEXT GO provides a central service hub where users can access the relevant module, submit requests and monitor their activities.

Each module maintains its own business logic while participating in the integrated application. This structure supports independent development and helps keep responsibilities clear across the project.

## Core Modules

| Module                       | Key Capabilities                                                                                                                                                   |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Ride Management**          | Map-based pickup and destination selection, vehicle selection, fare calculation, driver availability, ride acceptance, tracking and passenger booking history.     |
| **Courier Delivery**         | Parcel booking, delivery partner workflows, tracking codes, delivery status updates, location tracking and delivery history.                                       |
| **School Student Transport** | Parent and student records, bus and route information, transport schedules, arrival notifications and simulated vehicle tracking.                                  |
| **Food & Grocery**           | Product browsing, shopping cart, order placement, customer order history, vendor management and delivery partner workflows.                                        |
| **Vehicle Rental**           | Vehicle browsing, date-based availability, rental pricing, booking management and rental extensions.                                                               |
| **Emergency Services**       | Requests for ambulance, fire, police and emergency agents; map-based location selection; tracking codes; location updates; and request cancellation with a reason. |

## Key Features

* **Central service hub:** Access all six modules through one application.
* **User-specific records:** View bookings and activity associated with the relevant user account.
* **Separate driver access:** Maintain dedicated ride-driver authentication and portal workflows.
* **Interactive maps:** Select locations and display geographical information.
* **Status-based workflows:** Follow requests through their operational stages.
* **Persistent storage:** Store application records in Microsoft SQL Server.
* **Input validation:** Validate request data before processing.
* **Light and dark themes:** Support different viewing preferences across the integrated interfaces.
* **Emergency access:** Submit emergency requests without requiring a standard customer login.

## Technology Stack

| Layer                               | Technologies                                                   |
| ----------------------------------- | -------------------------------------------------------------- |
| **Backend**                         | Java 17, Spring Boot 3.2.5, Spring Web MVC                     |
| **Persistence**                     | Spring Data JPA, Hibernate, Microsoft SQL Server               |
| **Frontend**                        | JSP, JSTL, HTML, CSS, JavaScript, Bootstrap 5                  |
| **Authentication & Access Control** | Spring Security, HTTP Sessions, BCrypt, Courier JWT components |
| **Maps & Location**                 | Leaflet, OpenStreetMap, Nominatim, Browser Geolocation API     |
| **Real-Time Messaging**             | Spring WebSocket, STOMP, SockJS                                |
| **Supporting Libraries**            | Jakarta Bean Validation, Jackson, Lombok, Spring Boot Mail     |
| **Build & Packaging**               | Apache Maven, Maven Wrapper, WAR                               |
| **Testing**                         | JUnit 5, Spring Boot Test, MockMvc, Spring Security Test       |
| **Version Control**                 | Git and GitHub                                                 |

## Architecture

NEXT GO follows a layered Spring MVC architecture:

1. **Presentation layer** — JSP pages, stylesheets and JavaScript provide the user interface.
2. **Controller layer** — Handles page navigation and HTTP requests.
3. **Service layer** — Implements business rules, validation and workflow operations.
4. **Repository layer** — Provides access to persistent application data.
5. **Database layer** — Stores records in Microsoft SQL Server.

The main application entry point is `WebTransApplication.java`.

Module packages are organised under `src/main/java/com/webtrans`, JSP views under `src/main/webapp/WEB-INF/jsp`, and shared or module-specific assets under `src/main/resources/static`.

## Software Engineering Concepts

### Object-Oriented Programming

The implementation uses classes and interfaces to represent domain entities and service responsibilities. Encapsulation, abstraction, inheritance and polymorphism are applied where appropriate, while Spring dependency injection connects application components.

### Design Patterns

Selected examples include:

* **Strategy — Ride fare calculation:** Different vehicle categories use separate fare calculation strategies.
* **Observer — School transport notifications:** Transport events notify registered observers.
* **Simple Factory — Emergency service selection:** Service-specific behaviour is selected according to the requested emergency type.
* **Strategy and Observer — Courier services:** Pricing strategies and parcel-status observers separate calculation and event-handling responsibilities.

### CRUD Operations

The modules provide create, read, update and delete or cancellation operations where appropriate. Operational records may use status changes or soft deletion to retain history.

## Running the Project

### Prerequisites

* JDK 17
* Microsoft SQL Server
* Git
* IntelliJ IDEA or another Java-compatible IDE
* Internet access for Maven dependencies and external map services

### Setup

1. Clone this repository.
2. Open the folder containing `pom.xml`.
3. Reload the Maven project.
4. Create or select a SQL Server database.
5. Configure the database connection and required environment settings.
6. Run `WebTransApplication` or use the Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

Open the application using the port configured by `server.port`.

Keep database passwords, mail credentials and signing secrets in local configuration or environment variables rather than committing them to Git.

## Testing

Run the test suite using:

```powershell
.\mvnw.cmd test
```

Integration tests may require a correctly configured test database and supporting settings. Review the test output before treating a build as verified.

Manual verification should cover authentication, record ownership, booking creation, status transitions, cancellation, map interactions and navigation between modules.

## Project Scope

NEXT GO is an academic prototype demonstrating integrated service workflows.

School vehicle movement includes simulated GPS data. External map services and email functionality depend on their configuration and availability. Emergency requests demonstrate an application workflow and are not connected to official emergency dispatch services.

## Collaboration

The project was developed collaboratively, with responsibilities divided across the six main modules. Git and GitHub support version control and integration.

Before merging changes, contributors should review affected files, preserve other modules’ behaviour, avoid committing generated files or credentials, and verify the relevant workflows.

---

**NEXT GO**
*Integrated mobility and service management, developed through collaborative software engineering.*
