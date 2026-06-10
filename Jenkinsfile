pipeline {
    agent any

    environment {
        DOCKER_HUB_USER = 'your-dockerhub-username' // 🚨 Replace with your real Docker Hub username!
        IMAGE_NAME      = 'shipment-service'
        REGISTRY_IMAGE  = "docker.io/${DOCKER_HUB_USER}/${IMAGE_NAME}"
    }

    stages {
        stage('Build & Test') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Trivy FS Scan') {
            steps {
                sh 'trivy fs --severity HIGH,CRITICAL --exit-code 0 .'
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${REGISTRY_IMAGE}:${BUILD_NUMBER} -t ${REGISTRY_IMAGE}:latest ."
            }
        }

        stage('Trivy Image Scan') {
            steps {
                sh "trivy image --severity HIGH,CRITICAL --exit-code 0 ${REGISTRY_IMAGE}:${BUILD_NUMBER}"
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh "echo ${PASS} | docker login -u ${USER} --password-stdin"
                    sh "docker push ${REGISTRY_IMAGE}:${BUILD_NUMBER}"
                    sh "docker push ${REGISTRY_IMAGE}:latest"
                }
            }
        }

        stage('Deploy Stack') {
            steps {
                sh 'docker compose down'
                sh 'docker compose up -d'
            }
        }
    }
}
