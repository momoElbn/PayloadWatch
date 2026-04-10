# PayloadWatch 🛡️

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.5-6DB33F?style=for-the-badge&logo=spring-boot)
![AWS](https://img.shields.io/badge/AWS-Cloud_Native-232F3E?style=for-the-badge&logo=amazon-aws)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Ready-336791?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker)

**PayloadWatch** is a cloud-native API observability platform designed to catch silent data failures that standard uptime monitors miss.

While traditional monitors only verify `HTTP 200 OK` status codes, PayloadWatch acts as an automated contract enforcer. It utilizes an autonomous Spring Boot polling engine to continuously evaluate external API responses against user-defined JSON contracts. By validating the presence of critical data fields in real-time, it guarantees backend reliability and prevents broken payloads from reaching the client.

https://github.com/user-attachments/assets/2323d195-381c-4b89-9861-b0b434435224

---

## ✨ Key Features

* **JSON Contract Validation:** Goes beyond basic pinging by actively parsing response payloads to ensure required data keys and types are present.
* **Autonomous Polling Engine:** A `@Scheduled` Spring Boot worker that manages its own execution cycles and database querying without blocking main threads.
* **Stateless Security:** Secures all REST endpoints utilizing AWS Cognito for JWT-based identity management and route protection.
* **Lightweight Dashboard:** A blazing-fast, framework-free Vanilla JavaScript Single Page Application (SPA) for managing monitors and viewing health metrics.

---

## 🏗️ Architecture & Tech Stack


### Backend & Core Engine

* **Java 21 & Spring Boot 4.0.5:** Core REST API and background task scheduling.
* **Spring Data JPA & Hibernate:** ORM for database interactions.
* **Jackson:** High-performance JSON parsing.


### ☁️ Cloud Infrastructure (AWS)
PayloadWatch is designed for high availability and is fully deployed on a serverless AWS architecture:

**Networking & Traffic Routing:**
  
  * **Route 53 & ACM:** Custom domain routing (`payload-watch.com`) secured with free public SSL certificates via AWS Certificate Manager.
  * **Application Load Balancer (ALB):** Acts as the single public entry point, forcing HTTPS redirection and intelligently routing traffic only to healthy containers.
  * **VPC & Security Groups:** A strict "Circle of Trust" firewall architecture. The ALB is the only resource exposed to the internet, while the compute and database layers reside in isolated subnets.

**Compute Layer (Serverless):**
  
  * **Amazon ECS & AWS Fargate:** The containerized Spring Boot application runs on Fargate Spot, providing serverless compute without the overhead of managing EC2 instances. 
  * **Amazon ECR:** Private registry hosting the compiled AMD64 Docker images.

**Data & Auth Layer:**
  
  * **Amazon RDS:** Managed PostgreSQL database handling persistent storage for contracts and health logs.
  * **Amazon Cognito:** Manages SSO, user registration, and issues JWT tokens for secure API access.

**Monitoring:**
  
  * **CloudWatch:** Captures all application logs and container metrics for real-time debugging and monitoring.

<img width="1454" height="898" alt="Cloud-Infrastructure_PayloadWatch" src="https://github.com/user-attachments/assets/e7fa2417-8d8f-43e0-aa51-b9040510fdce" />


### 🗄️ Database Architecture
PayloadWatch is built on a fully normalized **PostgreSQL** relational database. The schema separates core user data from the high-volume background logging engine to ensure scalable polling performance.

<img width="886" height="795" alt="Database-Diagram" src="https://github.com/user-attachments/assets/020f0a48-a2b8-4919-b6e4-d12b2e9c9202" />

---

## 🚀 Getting Started (Local Development)

You can spin up the entire PayloadWatch backend and database in seconds using Docker Compose. 

### Prerequisites
* **Docker Desktop** installed and running.
* An **AWS Account** (for Cognito authentication).
  * **Note on Authentication:** PayloadWatch uses AWS Cognito for secure, stateless identity management. To run this locally, you'll need to create a User Pool in your own AWS account and update the .env file with your ISSUER_URI and CLIENT_ID. This ensures your local development data remains entirely isolated and under your control.

### 1. Clone the Repository
```bash
git clone https://github.com/momoElbn/PayloadWatch.git
cd PayloadWatch
```

### 2. Configure Environment Variables
PayloadWatch centralizes its configuration to make local development and open-source use seamless.
Create a .env file in the root directory and populate it with your AWS and Database credentials:
```.env
# .env.example
# Copy this file to .env and replace the placeholders with your own values

DB_NAME=your_database_name
DB_URL=jdbc:postgresql://db:5432/${DB_NAME}
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password

# AWS Cognito Configuration
# Example: https://cognito-idp.us-east-1.amazonaws.com/us-east-1_abcd12345
COGNITO_ISSUER_URI=https://cognito-idp.[region].amazonaws.com/[your_pool_id]

# Example: 7abc1234567890defghijk
COGNITO_CLIENT_ID=your_cognito_app_client_id

# Infrastructure Region
AWS_REGION=your_aws_region
```

### 3. Spin up the Docker Container
```bash
docker-compose up -d --build
```

### 4. Access the application
``http://localhost:8080``

### 💡 Pro-Tip: Monitoring Local APIs
If you are running an API locally on your machine (e.g., on port 5000) and want PayloadWatch to monitor it, do not use localhost in the monitor URL. Since PayloadWatch is running inside a container, localhost refers to the container itself.

Instead, use the Docker bridge address:

Target URL: `http://host.docker.internal:5000/api/your-endpoint`

### 5. Stop the application
```bash
docker-compose down
```

## 🔮 Future Updates
The following features are currently in active development and will be rolled out in upcoming patches:

### Event-Driven Alerting (Amazon SES): 
Automated, low-latency email notifications dispatched the moment the engine detects a breached JSON contract or degraded API performance.

### Subscription Tiers: 
Implementation of tiered account usage, unlocking premium features such as higher-frequency polling intervals and extended monitor limit.

---

## 👨‍💻 Let's Connect

Developed by **Mohammed El Ouaabani** SWE @ Concordia University

[![LinkedIn](https://img.shields.io/badge/LinkedIn-%230077B5.svg?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/mohammed-el-ouaabani-808014384)
