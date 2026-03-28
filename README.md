# 🚀 Zudio App CI/CD Pipeline (Jenkins + Docker + Argo CD)

A complete end-to-end example of a GitOps-driven CI/CD workflow using Jenkins, Docker, Kubernetes, and Argo CD.

## Table of Contents

- [Project Overview](#project-overview)
- [Project Structure](#project-structure)
- [CI/CD Flow](#cicd-flow)
- [Dockerfile](#dockerfile)
- [Jenkinsfile (Windows)](#jenkinsfile-windows)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Access Application](#access-application)
- [Argo CD](#argo-cd)
- [Key Notes](#key-notes)
- [Troubleshooting](#troubleshooting)

## Project Overview

This project demonstrates an automated pipeline:
- commit code → Jenkins builds Docker image → push to Docker Hub
- update Kubernetes manifests → commit
- Argo CD detects Git change → deploy to Kubernetes

## Project Structure

```
.
├── Dockerfile
├── Jenkinsfile
├── zudio-website.zip
├── k8s/
│   ├── deployment.yaml
│   └── service.yaml
└── job-dsl/
    └── seed.groovy
```

## CI/CD Flow

1. GitHub commit / webhook trigger
2. Jenkins pulls source
3. Build Docker image
4. Push image to Docker Hub
5. Update Kubernetes deployment manifest with new image tag
6. Commit manifest changes
7. Argo CD syncs and deploys to cluster

## Dockerfile

```dockerfile
FROM httpd:2.4

LABEL Name="Ramesh Aravind" Version="v1.0.0"

WORKDIR /usr/local/apache2/htdocs

COPY zudio-website.zip .

RUN apt-get update \
    && apt-get install -y unzip \
    && unzip zudio-website.zip \
    && rm zudio-website.zip

EXPOSE 80
CMD ["httpd-foreground"]
```

## Jenkinsfile (Windows)

```groovy
pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "ramesh0112/zudio-app"
        TAG = "${BUILD_NUMBER}"
        DOCKER_CREDENTIALS_ID = "docker-creds"
    }

    stages {
        stage('Build Docker Image') {
            steps {
                bat "docker build -t %DOCKER_IMAGE%:%TAG% ."
            }
        }

        stage('Login to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKER_CREDENTIALS_ID}",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat 'docker login -u %DOCKER_USER% -p %DOCKER_PASS%'
                }
            }
        }

        stage('Push Image') {
            steps {
                bat '''
                docker push %DOCKER_IMAGE%:%TAG%
                docker tag %DOCKER_IMAGE%:%TAG% %DOCKER_IMAGE%:latest
                docker push %DOCKER_IMAGE%:latest
                '''
            }
        }
    }
}
```

## Kubernetes Deployment

`k8s/deployment.yaml` and `k8s/service.yaml` define the app deployment and internal service.

- Ensure `image` in `deployment.yaml` is updated to the current tag before commit.
- Example: `ramesh0112/zudio-app:<TAG>`

## Access Application

Use port-forwarding:

```bash
kubectl port-forward svc/zudio-app-service 8080:80
```

Open: `http://localhost:8080`

## Argo CD

- Argo CD watches this repo (or configured manifest path).
- On Git manifest change, it syncs and deploys to cluster.

## Key Notes

- Argo CD tracks Git manifests, not Docker image registry directly.
- Always commit image tag updates for deterministic deployments.
- Avoid using `latest` in production.

## Troubleshooting

```bash
kubectl get pods
kubectl describe pod <pod>
kubectl logs <pod>
```

## Summary

End-to-end CI/CD workflow for the Zudio app with Jenkins, Docker, Kubernetes, and Argo CD.
