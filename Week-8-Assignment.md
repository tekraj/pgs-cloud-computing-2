# Cloud Architecture Assignment: Single-Instance Production Environment Setup

## Objective
Configure a highly secure, available, and scalable cloud-native infrastructure within AWS. You will deploy a single EC2 instance hosting a containerized application via Docker Compose, isolated behind an Application Load Balancer (ALB), and connected to an Amazon RDS MySQL instance deployed via 'Easy Create'.

---

## Technical Specifications & Reference Data
Use the following network configurations derived from the architecture diagram for your implementation:

* **Regional VPC CIDR:** `10.0.0.0/16`
* **Public Subnet A (us-east-1a):** `10.0.1.0/24`
* **Public Subnet B (us-east-1b):** `10.0.2.0/24`
* **App Subnet A (us-east-1a):** `10.0.10.0/24`
* **App Subnet B (us-east-1b):** `10.0.11.0/24` *[Optional reference from diagram]*

---

## Tasks & Requirements

### Task 1: Network Core Infrastructure
1. Create a custom **VPC** with the regional CIDR block specified above.
2. Provision **four (4) subnets** within this VPC:
   * Two public subnets dedicated to the Application Load Balancer (ALB) across two distinct Availability Zones (`us-east-1a` and `us-east-1b`).
   * Two private/app subnets across the different Availability Zones to prepare for compute routing.
3. Deploy an **Internet Gateway (IGW)** and attach it to your custom VPC.
4. Configure a **Public Route Table** associated with the two ALB public subnets that routes all external traffic (`0.0.0.0/0`) through the Internet Gateway.

### Task 2: Security Group Configuration
1. **ALB Security Group (`ALB SG`):**
   * Allow inbound `HTTP (80)` and `HTTPS (443)` traffic from `Anywhere (0.0.0.0/0)`.
2. **EC2 Security Group (`EC2 SG`):**
   * Allow inbound `SSH (22)` from your local  IP.
   * Allow inbound `HTTP` traffic **only** when originating from the `ALB SG` (Security Group referencing).

### Task 3: Compute Instance Deployment
1. Launch **one (1) EC2 Instance** (Ubuntu) inside `App Subnet A`.
2. Enable/assign a **Public IP** to this instance to allow administrative SSH connectivity during setup.
3. Associate the instance with the `EC2 SG` security group created in Task 2.

### Task 4: Managed Database Deployment
1. Navigate to Amazon RDS and create a new **MySQL Database** using **Easy Create**.
2. Select the **Free Tier** template option.
3. Inside the database configuration, explicitly choose to **Connect to an EC2 compute resource** and select the EC2 instance created in Task 3.

### Task 5: Application Provisioning & Containerization
1. Establish an SSH connection to your EC2 instance.
2. Clone the https://github.com/tekraj/pgs-cloud-computing-2# repository to the server.
3. Locate the initialization script `aws-setup.sh` inside the repository. Modify its permissions to make it executable and execute it to install all system dependencies (including Docker and Docker Compose).
4. Open the `docker-compose.yml` file and update the environment variables with your provisioned RDS MySQL database endpoint, username, password, and database name.
5. Launch the application in detached mode using `docker-compose build ` and then `docker compose up -d`.

### Task 6: Routing & Load Balancing
1. Create a **Target Group** (`Main TG`) targeting instance types on the same custom VPC.
2. Register your active EC2 Web Server instance into this Target Group on the application's listening port.
3. Create an **Application Load Balancer (ALB)** (`Main ALB`) across the two Public Subnets configured in Task 1.
4. Attach the `ALB SG` security group to the ALB.
5. Define a routing listener rule on the ALB to forward incoming `HTTP (80)` traffic directly to your `Main TG` Target Group.

---

## Submission Deliverables
* **ALB Endpoints:** Provide the fully qualified public DNS name of your Application Load Balancer.
* **Verification:** Verify that the web application renders correctly through the ALB URL and successfully queries/mutates the connected MySQL backend database.
