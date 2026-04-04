# PayloadWatch 🛡️

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0-6DB33F?style=for-the-badge&logo=spring-boot)
![AWS](https://img.shields.io/badge/AWS-Cloud_Native-232F3E?style=for-the-badge&logo=amazon-aws)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Ready-336791?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker)

**PayloadWatch** is a cloud-native API observability platform designed to catch silent data failures that standard uptime monitors miss.

While traditional monitors only verify `HTTP 200 OK` status codes, PayloadWatch acts as an automated contract enforcer. It utilizes an autonomous Spring Boot polling engine to continuously evaluate external API responses against user-defined JSON schemas. By validating structural integrity and the presence of critical data fields in real-time, it guarantees backend reliability and prevents broken payloads from reaching the client.

---

## ✨ Key Features

* **JSON Contract Validation:** Goes beyond basic pinging by actively parsing response payloads via Jackson to ensure required data keys and types are present.
* **Autonomous Polling Engine:** A multithreaded `@Scheduled` Spring Boot worker that manages its own execution cycles and database querying without blocking main threads.
* **Stateless Security:** Secures all REST endpoints utilizing AWS Cognito for JWT-based identity management and route protection.
* **Lightweight Dashboard:** A blazing-fast, framework-free Vanilla JavaScript (Vite) Single Page Application (SPA) for managing monitors and viewing health metrics.

---

## 🏗️ Architecture & Tech Stack

### Backend & Core Engine
* **Java 21 & Spring Boot 3:** Core REST API and background task scheduling.
* **Spring Data JPA & Hibernate:** ORM for database interactions.
* **Jackson:** High-performance JSON parsing.

### 🗄️ Database Architecture
PayloadWatch is built on a fully normalized **PostgreSQL** relational database. The schema separates core user data from the high-volume background logging engine to ensure scalable polling performance.

### ☁️ Cloud Infrastructure (AWS)
PayloadWatch is designed for high availability and is fully deployed on a serverless AWS architecture:

* **Networking & Traffic Routing:**
  * **Route 53 & ACM:** Custom domain routing (`payload-watch.com`) secured with free public SSL certificates via AWS Certificate Manager.
  * **Application Load Balancer (ALB):** Acts as the single public entry point, forcing HTTPS redirection and intelligently routing traffic only to healthy containers.
  * **VPC & Security Groups:** A strict "Circle of Trust" firewall architecture. The ALB is the only resource exposed to the internet, while the compute and database layers reside in isolated subnets.
* **Compute Layer (Serverless):**
  * **Amazon ECS & AWS Fargate:** The containerized Spring Boot application runs on Fargate Spot, providing serverless compute without the overhead of managing EC2 instances. 
  * **Amazon ECR:** Private registry hosting the compiled ARM64 Docker images.
* **Data & Auth Layer:**
  * **Amazon RDS:** Managed PostgreSQL database handling persistent storage for contracts and health logs.
  * **Amazon Cognito:** Manages SSO, user registration, and issues JWT tokens for secure API access.
* **Monitoring:**
  * **CloudWatch:** Captures all application logs and container metrics for real-time debugging and monitoring.

---

## 🚀 Getting Started (Local Development)

You can spin up the entire PayloadWatch backend and database in seconds using Docker Compose. 

### Prerequisites
* **Docker Desktop** installed and running.
* An **AWS Account** (for Cognito authentication).

### 1. Clone the Repository
```bash
git clone [https://github.com/momoElbn/PayloadWatch.git](https://github.com/momoElbn/PayloadWatch.git)
cd PayloadWatch
```

### 2. Configure Environment Variables
PayloadWatch centralizes its configuration to make local development and open-source use seamless.
Create a .env file in the root directory and populate it with your AWS and Database credentials:
```.env
# Database Configuration
DB_URL=jdbc:postgresql://db:5432/postgres
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# AWS Cognito Configuration
COGNITO_ISSUER_URI=[https://cognito-idp.ca-central-1.amazonaws.com/your_pool_id](https://cognito-idp.ca-central-1.amazonaws.com/your_pool_id)
COGNITO_CLIENT_ID=your_app_client_id

# AWS Configuration (For general clients)
AWS_REGION=ca-central-1
```

### 3. Spin up the Docker Container
```bash
docker-compose up -d --build
```

### 4. Access the application
``http://localhost:8080``

### 5. Stop the application
```bash
docker-compose down
```

## 🔮 Future Updates
The following features are currently in active development and will be rolled out in upcoming patches:

### Event-Driven Alerting (Amazon SES): 
Automated, low-latency email notifications dispatched the moment the engine detects a breached JSON contract or degraded API performance.

### Subscription Tiers: 
Implementation of tiered account usage, unlocking premium features such as higher-frequency polling intervals and extended historical health analytics.
