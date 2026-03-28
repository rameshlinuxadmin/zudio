# 🚀 Zudio App CI/CD Pipeline (Jenkins + Docker + Argo CD)

This project demonstrates a complete CI/CD pipeline using:

- Jenkins (CI)
- Docker (Image build & push)
- Docker Hub (Image registry)
- Kubernetes (Docker Desktop)
- Argo CD (GitOps deployment)
- Job DSL (Jenkins automation)

---

# 📁 Project Structure

.
├── Dockerfile
├── jenkinsfile
├── zudio-website.zip
├── k8s/
│   ├── deployment.yaml
│   └── service.yaml
├── job-dsl/
│   └── seed.groovy

---

# ⚙️ CI/CD Flow

GitHub Commit
     ↓
Jenkins (Poll SCM / Trigger)
     ↓
Build Docker Image
     ↓
Push to Docker Hub
     ↓
Update Kubernetes YAML (image tag)
     ↓
Commit to GitHub
     ↓
Argo CD detects change
     ↓
Deploys to Kubernetes

---

# 🐳 Dockerfile

FROM httpd:2.4

LABEL Name="Ramesh Aravind" Version="v1.0.0"

WORKDIR /usr/local/apache2/htdocs

COPY zudio-website.zip .

RUN apt-get update && apt-get install -y unzip     && unzip zudio-website.zip     && rm zudio-website.zip

EXPOSE 80

CMD ["httpd-foreground"]

---

# 🔧 Jenkinsfile (Windows)

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

---

# ☸️ Kubernetes Deployment

deployment.yaml and service.yaml define the application and expose it internally.

---

# 🌐 Access Application

kubectl port-forward svc/zudio-app-service 8080:80

Open: http://localhost:8080

---

# 🚀 Argo CD Application

Argo CD tracks Git repo and deploys automatically when changes occur.

---

# ⚠️ Key Notes

- Argo CD tracks Git, not Docker images
- Always update image tag in Git
- Avoid using latest tag in production

---

# 🐛 Troubleshooting

kubectl get pods  
kubectl describe pod <pod>  
kubectl logs <pod>

---

# ✅ Summary

End-to-end CI/CD using Jenkins, Docker, and Argo CD with GitOps workflow.
