pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    environment {
        // Change this to your actual Docker Hub username/namespace
        DOCKER_HUB_USER = 'your-dockerhub-username' 
        IMAGE_NAME      = 'shipment-service'
        REGISTRY_IMAGE  = "docker.io/${DOCKER_HUB_USER}/${IMAGE_NAME}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                // Skipping tests here since they run explicitly in the next stage
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        // --- NEW: SONARQUBE ANALYSIS STAGE ---
        stage('SonarQube Analysis') {
            steps {
                // 'SonarQube' must match the Server name configured in Manage Jenkins -> System
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        // --- NEW: TRIVY FILE SYSTEM SCAN STAGE ---
        stage('Trivy FS Scan') {
            steps {
                echo 'Scanning code directory for vulnerability vulnerabilities...'
                sh 'trivy fs --severity HIGH,CRITICAL --exit-code 0 .'
            }
        }

        stage('Docker Build') {
            steps {
                sh """
                docker build \
                -t ${REGISTRY_IMAGE}:${BUILD_NUMBER} \
                -t ${REGISTRY_IMAGE}:latest .
                """
            }
        }

        // --- NEW: TRIVY IMAGE SCAN STAGE ---
        stage('Trivy Image Scan') {
            steps {
                echo "Scanning generated Docker image for vulnerabilities..."
                sh "trivy image --severity HIGH,CRITICAL --exit-code 0 ${REGISTRY_IMAGE}:${BUILD_NUMBER}"
            }
        }

        // --- NEW: DOCKER HUB PUSH STAGE ---
        stage('Docker Push') {
            steps {
                // 'docker-hub-credentials' must match the ID created in your Jenkins Credentials Store
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh "echo ${PASS} | docker login -u ${USER} --password-stdin"
                    sh "docker push ${REGISTRY_IMAGE}:${BUILD_NUMBER}"
                    sh "docker push ${REGISTRY_IMAGE}:latest"
                }
            }
        }

        // --- NEW: DOCKER COMPOSE DEPLOYMENT STAGE ---
        stage('Deploy Stack') {
            steps {
                echo 'Redeploying microservice environment via Docker Compose...'
                // Forces Compose to cycle the containers cleanly using the updated images
                sh 'docker compose down'
                sh 'docker compose up -d'
            }
        }
    }

    post {
        always {
            echo 'Wiping build workspace resources...'
            cleanWs()
        }
    }
}
