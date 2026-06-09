#!/bin/bash

# 1. Uninstall old versions
apt remove -y docker.io docker-compose docker-compose-v2 docker-doc podman-docker containerd runc

# 2. Set up Docker's apt repository
apt update
apt install -y ca-certificates curl
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Apt sources
echo \
  "Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc" | tee /etc/apt/sources.list.d/docker.sources > /dev/null

apt update

# 3. Install the Docker packages
# This includes the docker-compose-plugin, which is the current standard for Docker Compose
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 4. Ensure Docker is started
systemctl start docker