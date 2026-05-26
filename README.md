# AWS EC2, VPC, RDS MySQL, and S3 Access Deployment Guide

This guide walks you through creating a secure AWS network, launching an EC2 instance, creating a private MySQL RDS database, granting S3 permissions to the EC2 IAM role, cloning the repository, and running the application with Docker Compose.

---



## Step 1: Add S3 Permissions to IAM Role (LabRole)

Do this first so the correct role is available when launching EC2.

1. Go to **AWS Management Console -> IAM**.
2. Click **Roles** on the left sidebar.
3. Search for and select **LabRole** (or the role your instructor provided).
4. Click **Add permissions**.
5. Select **Attach policies directly**.
6. Search for `AmazonS3FullAccess`.
7. Check the box next to **AmazonS3FullAccess**.
8. Click **Add permissions**.

---

## Step 2: Create a Custom VPC with Public and Private Subnets

Create the network first so EC2 and RDS are placed correctly.

1. Open **AWS Console -> VPC -> Your VPCs -> Create VPC**.
2. Select **VPC only** and create:
   * **Name:** `pgs-vpc`
   * **IPv4 CIDR:** `10.0.0.0/16`
3. Go to **Subnets -> Create subnet** and create 4 subnets in this VPC:
   * `pgs-public-subnet-1` -> `10.0.1.0/24` (AZ 1)
   * `pgs-public-subnet-2` -> `10.0.2.0/24` (AZ 2)
   * `pgs-private-subnet-1` -> `10.0.11.0/24` (AZ 1)
   * `pgs-private-subnet-2` -> `10.0.12.0/24` (AZ 2)
4. Open **Internet Gateways -> Create internet gateway**:
   * **Name:** `pgs-igw`
   * Attach it to `pgs-vpc`.

### Route Tables

1. Create a route table named `pgs-public-rt` in `pgs-vpc`.
2. Add route: `0.0.0.0/0` -> `pgs-igw`.
3. Associate `pgs-public-rt` with:
   * `pgs-public-subnet-1`
   * `pgs-public-subnet-2`
4. Create a route table named `pgs-private-rt` in `pgs-vpc`.
5. Associate `pgs-private-rt` with:
   * `pgs-private-subnet-1`
   * `pgs-private-subnet-2`

Note: Private subnets should not have a direct route to the Internet Gateway.

---

## Step 3: Create Security Groups in the VPC

Create separate security groups for EC2 and RDS.

1. Go to **EC2 -> Security Groups -> Create security group**.
2. Create `pgs-ec2-sg` in `pgs-vpc` with inbound rules:
   * SSH (22) from **My IP** (recommended)
   * HTTP (80) from `0.0.0.0/0`
   * HTTPS (443) from `0.0.0.0/0`
3. Create `pgs-rds-sg` in `pgs-vpc` with inbound rule:
   * MySQL/Aurora (3306) source = **security group** `pgs-ec2-sg`

This ensures the database is reachable only from EC2 instances in `pgs-ec2-sg`.

---

## Step 4: Launch an EC2 Instance in a Public Subnet (Free Tier)

1. Open **EC2 Dashboard -> Launch Instance**.
2. **Name:** `pgs-app-server`.
3. **AMI:** Ubuntu Server 24.04 LTS or 22.04 LTS (Free Tier eligible).
4. **Instance type:** `t2.micro` (or `t3.micro` depending on region).
5. **Key pair:** Select existing or create a new one.
6. **Network settings:**
   * **VPC:** `pgs-vpc`
   * **Subnet:** `pgs-public-subnet-1` (or `pgs-public-subnet-2`)
   * **Auto-assign public IP:** Enable
   * **Security group:** Select existing `pgs-ec2-sg`
7. Under **Advanced details -> IAM instance profile**, select **LabRole** (or your instructor-provided role with S3 access).
8. Launch the instance.

---

## Step 5: Create an RDS MySQL Database in Private Subnets (Free Tier)

1. Open **RDS Dashboard -> Create database**.
2. **Creation method:** Standard create.
3. **Engine type:** MySQL.
4. **Template:** Free Tier.
5. **DB instance identifier:** `pgs-mysql-db`.
6. Set master username/password.
7. Keep instance class as `db.t3.micro` (or `db.t2.micro`).
8. Under **Connectivity**:
   * **VPC:** `pgs-vpc`
   * **Public access:** **No**
   * **VPC security group:** Select `pgs-rds-sg`
   * **DB subnet group:** Use private subnets (`pgs-private-subnet-1` and `pgs-private-subnet-2`)
9. Create database.

Important: The DB must be in private subnets and must not be publicly accessible.

---

## Step 6: Access EC2, Clone Repo, and Install Docker

1. Change permissions for your private key:
   ```bash
   chmod 400 /path/to/your-key.pem
   ```
2. SSH into EC2:
   ```bash
   ssh -i /path/to/your-key.pem ubuntu@<EC2_PUBLIC_IP>
   ```
3. Update packages and install Docker + Docker Compose plugin:
   ```bash
   sudo apt update && sudo apt upgrade -y
   sudo apt install -y docker.io docker-compose-plugin git
   sudo usermod -aG docker $USER
   newgrp docker
   ```
4. Clone your repository:
   ```bash
   git clone <YOUR_REPOSITORY_URL>
   cd pgs-cloud-2
   ```

---

## Step 7: Configure Application Environment

Before starting containers, make sure your app points to the private RDS endpoint.

1. Copy the **RDS endpoint** from your RDS instance details.
2. Update your environment/configuration values (for example in `.env`, `docker-compose.yml`, or app config) with:
   * DB host = `<RDS_ENDPOINT>`
   * DB port = `3306`
   * DB username/password = the credentials you set in RDS

---

## Step 8: Start the Application with Docker Compose

From the project root on EC2:

```bash
docker compose up -d --build
```

Check containers:

```bash
docker compose ps
```

View logs if needed:

```bash
docker compose logs -f
```

---

## Security Checklist (Recommended)

* EC2 is deployed in a **public subnet** with controlled inbound access.
* RDS is deployed in **private subnets** only.
* RDS **Public access = No**.
* RDS security group allows MySQL `3306` **only from EC2 security group**.
* IAM role attached to EC2 includes S3 permissions needed for audio uploads.