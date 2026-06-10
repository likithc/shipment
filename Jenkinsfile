pipeline {
    agent any

    // We removed the 'tools' block completely to fall back to your native system paths!

    environment {
        DOCKER_HUB_USER = 'your-dockerhub-username' // Update to your actual Docker Hub username
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
                // This calls the native 'mvn' installed on your system
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Trivy FS Scan') {
            steps {
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

    post {
        always {
            cleanWs()
        }
    }
}
