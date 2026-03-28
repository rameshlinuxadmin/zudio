# 🚀 Zudio App CI/CD Pipeline (Jenkins + Docker + Argo CD)

A complete end-to-end GitOps CI/CD example using Jenkins, Docker Hub, Kubernetes (Docker Desktop), and Argo CD.

## Table of Contents

- [Project Overview](#project-overview)
- [Repository Structure](#repository-structure)
- [CI/CD Flow](#cicd-flow)
- [Docker Image Build](#docker-image-build)
- [Jenkins Pipeline](#jenkins-pipeline)
- [Jenkins Job DSL](#jenkins-job-dsl)
- [Kubernetes Manifest](#kubernetes-manifest)
- [Run Locally](#run-locally)
- [Argo CD Integration](#argo-cd-integration)
- [Key Notes](#key-notes)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## Project Overview

This repository implements a sample web app CI/CD pipeline:
- Build an Apache HTTP server Docker image containing static website content.
- Push image to Docker Hub with versioned and latest tags.
- Update Kubernetes manifests with immutable image tags.
- Argo CD monitors Git and deploys changes into cluster.

## Repository Structure

```
.
├── Dockerfile
├── Jenkinsfile
├── README.md
├── zudio-website.zip
├── k8s/
│   ├── zudio-app.yaml
│   └── zudio-service.yaml
└── job-DSL/
    └── seed.groovy
```

## CI/CD Flow

1. Git push -> Jenkins poll SCM trigger (or webhook).
2. Jenkins checks out `main` and builds the Docker image.
3. Jenkins logs into Docker Hub and pushes tags: `build_number` + `latest`.
4. (Optional) Update `k8s/zudio-app.yaml` image tag from `latest` to the new tagged image.
5. Commit manifest as GitOps source.
6. Argo CD detects repo manifest change and applies to Kubernetes.

## Docker Image Build

`Dockerfile`:

```dockerfile
FROM httpd
LABEL Name="Ramesh Aravind" Version="v1.0.0"
WORKDIR /usr/local/apache2/htdocs
COPY zudio-website.zip .
RUN apt-get update && apt-get install -y unzip \
    && unzip zudio-website.zip \
    && rm zudio-website.zip
EXPOSE 80
CMD ["httpd-foreground"]
```

- Base image: `httpd` (Apache).
- App files extracted from `zudio-website.zip` into web root.

## Jenkins Pipeline

`jenkinsfile`:

```groovy
pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "ramesh0112/zudio-app"
        TAG = "${BUILD_NUMBER}"
        REPO_URL = "https://github.com/rameshlinuxadmin/zudio.git"
        GIT_CREDENTIALS_ID = "github"
        DOCKER_CREDENTIALS_ID = "docker-creds"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: "${GIT_CREDENTIALS_ID}",
                    url: "${REPO_URL}"
            }
        }

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
                    bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                }
            }
        }

        stage('Push Image') {
            steps {
                bat """
                docker push %DOCKER_IMAGE%:%TAG%
                docker tag %DOCKER_IMAGE%:%TAG% %DOCKER_IMAGE%:latest
                docker push %DOCKER_IMAGE%:latest
                """
            }
        }
    }
}
```

### Parameters

- `DOCKER_IMAGE`: Docker Hub repo.
- `TAG`: Jenkins build number.
- `DOCKER_CREDENTIALS_ID`: Jenkins credentials ID for Docker Hub.
- `GIT_CREDENTIALS_ID`: Jenkins credentials ID for GitHub.

## Jenkins Job DSL

`job-DSL/seed.groovy` defines a pipeline job:

```groovy
pipelineJob('zudio-pipeline') {
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/rameshlinuxadmin/zudio.git')
                    }
                    branch('main')
                }
            }
            scriptPath('jenkinsfile')
        }
    }

    triggers {
        scm('H/2 * * * *')
    }
}
```

- Polls SCM every 2 minutes for changes.

## Kubernetes Manifest

`k8s/zudio-app.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: zudio-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: zudio-app
  template:
    metadata:
      labels:
        app: zudio-app
    spec:
      containers:
      - name: zudio-app
        image: ramesh0112/zudio-app:latest
        ports:
        - containerPort: 80
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "500m"
```

`k8s/zudio-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: zudio-app-service
spec:
  selector:
    app: zudio-app
  ports:
    - port: 80
      targetPort: 80
  type: ClusterIP
```

## Run Locally

1. Start Kubernetes cluster (Docker Desktop).
2. `kubectl apply -f k8s/zudio-app.yaml`
3. `kubectl apply -f k8s/zudio-service.yaml`
4. `kubectl port-forward svc/zudio-app-service 8080:80`
5. Open `http://localhost:8080`

## Argo CD Integration

- Configure Argo CD app to point at this repository path.
- Set refresh mode to automatic or polling.
- Use GitOps pattern: PB state is `k8s/` manifests.

> update `k8s/zudio-app.yaml` image to `ramesh0112/zudio-app:${TAG}` for deterministic deployment; avoid `latest` in production.

## Key Notes

- Keep Docker image tag in GitOps manifests.
- Archiving tags ensures traceable deployments.
- `latest` is acceptable for dev only; avoid in production.

## Troubleshooting

```bash
kubectl get pods
kubectl describe pod <pod>
kubectl logs <pod>
```

- Check Jenkins logs for build and push errors.
- Check Argo CD UI for sync/app health status.

## License

MIT License

Copyright (c) 2026 Ramesh Aravind

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction...