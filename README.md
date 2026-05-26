# AWS EC2, VPC, RDS MySQL, and S3 Access Deployment Guide

This guide walks you through creating a secure AWS network, launching an EC2 instance, creating a private MySQL RDS database, granting S3 permissions to the EC2 IAM role, cloning the repository, and running the application with Docker Compose.

---



## Step 1: Add S3 Permissions to IAM Role (LabRole)

Do this first so the correct role is available when launching EC2.

1. Go to **AWS Management Console -> IAM**.
2. Click **Roles** on the left sidebar.
3. Search for and select **LabInstance** .
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
7. Under **Advanced details -> IAM instance profile**, select **LatInstanceProfile** .
8. Launch the instance.

---

## Step 5: Create an RDS MySQL Database in Private Subnets (Free Tier)

1. First create a DB subnet group using the two private subnets:
   * Open **RDS -> Subnet groups -> Create DB Subnet Group**.
   * **Name:** `pgs-db-subnet-group`
   * **VPC:** `pgs-vpc`
   * Add subnets: `pgs-private-subnet-1` and `pgs-private-subnet-2`.
2. Open **RDS Dashboard -> Create database**.
3. **Creation method:** Standard create.
4. **Engine type:** MySQL.
5. **Template:** Free Tier.
6. **DB instance identifier:** `pgs-mysql-db`.
7. Set master username/password.
8. Under **Connectivity**:
   * **VPC:** `pgs-vpc`
   * **Public access:** **No**
   * **VPC security group:** Select `pgs-rds-sg`
   * **DB subnet group:** Select `pgs-db-subnet-group`
9. Create database.

Important: The DB must be in private subnets and must not be publicly accessible.

---

## Step 6: Access EC2, Clone Repo, and Install Docker with aws-setup.sh

1. Change permissions for your private key:
   ```bash
   chmod 400 /path/to/your-key.pem
   ```
2. SSH into EC2:
   ```bash
   ssh -i /path/to/your-key.pem ubuntu@<EC2_PUBLIC_IP>
   ```
3. Install Git, clone the project repository, and switch into it:
   ```bash
   sudo apt update
   sudo apt install -y git
   git clone https://github.com/tekraj/pgs-cloud-computing-2.git
   cd pgs-cloud-computing-2
   ```
4. Install Docker using the project setup script 
   ```bash
   chmod +x aws-setup.sh
   sudo bash aws-setup.sh --add-current-user
   newgrp docker
   ```

---

## Step 7: Update docker-compose.yml to Use RDS (No Local Docker MySQL)

Students must use the RDS database and not a Docker MySQL container.

1. Open `docker-compose.yml` in the cloned repository.
2. Keep the `mysql` service disabled (commented out), because DB is on RDS.
3. In the `app` service, remove or comment out the `depends_on: mysql` block.
4. Update these environment values in `app`:
   * `SPRING_DATASOURCE_URL=jdbc:mysql://<RDS_ENDPOINT>:3306/<DB_NAME>?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
   * `SPRING_DATASOURCE_USERNAME=<RDS_USERNAME>`
   * `SPRING_DATASOURCE_PASSWORD=<RDS_PASSWORD>`
5. Also update S3-related values if needed:
   * `S3_BUCKET_NAME=<YOUR_BUCKET_NAME>`
   * `AWS_REGION=<YOUR_AWS_REGION>`

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
* Docker is installed using `aws-setup.sh` from the repository, not manual package commands.