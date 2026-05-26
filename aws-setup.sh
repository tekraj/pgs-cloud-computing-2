#!/usr/bin/env bash
set -euo pipefail

# Installs Docker Engine + Docker Compose plugin on Ubuntu using Docker's official apt repository.
# Usage:
#   sudo bash aws-setup.sh
# Optional:
#   sudo bash aws-setup.sh --add-current-user

ADD_CURRENT_USER="false"
if [[ "${1:-}" == "--add-current-user" ]]; then
	ADD_CURRENT_USER="true"
fi

if [[ "${EUID}" -ne 0 ]]; then
	echo "Run this script as root (for example: sudo bash aws-setup.sh)."
	exit 1
fi

if [[ ! -f /etc/os-release ]]; then
	echo "Cannot determine OS: /etc/os-release not found."
	exit 1
fi

# shellcheck disable=SC1091
. /etc/os-release
if [[ "${ID:-}" != "ubuntu" ]]; then
	echo "This script supports Ubuntu only. Detected: ${ID:-unknown}."
	exit 1
fi

echo "Removing conflicting Docker-related packages if present..."
apt-get remove -y docker.io docker-compose docker-compose-v2 docker-doc podman-docker containerd runc || true

echo "Installing prerequisites..."
apt-get update
apt-get install -y ca-certificates curl

echo "Adding Docker GPG key..."
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

echo "Configuring Docker apt repository..."
cat >/etc/apt/sources.list.d/docker.sources <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: ${UBUNTU_CODENAME:-$VERSION_CODENAME}
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF

echo "Installing Docker Engine, CLI, containerd, Buildx, and Compose plugin..."
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

echo "Ensuring Docker service is enabled and running..."
systemctl enable --now docker

if [[ "${ADD_CURRENT_USER}" == "true" ]]; then
	if [[ -n "${SUDO_USER:-}" && "${SUDO_USER}" != "root" ]]; then
		usermod -aG docker "${SUDO_USER}"
		echo "Added ${SUDO_USER} to docker group. Log out/in (or run: newgrp docker) for changes to apply."
	else
		echo "Skipping docker group add: no non-root sudo user detected."
	fi
fi

echo
echo "Installation complete."
echo "Version check:"
docker --version
docker compose version
echo
echo "Optional test (pulls hello-world image):"
echo "  docker run --rm hello-world"
