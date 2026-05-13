pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = "kyntus-system-prod"
    }

    stages {
        stage('🧹 Clean & Checkout') {
            steps {
                script {
                    echo "=> [ÉTAPE 1] Nettoyage w téléchargement dyal l'Code..."
                    cleanWs()
                    checkout scm
                }
            }
        }

        stage('🚀 Build & Deploy (Compose)') {
            steps {
                script {
                    echo "=> [ÉTAPE 2] Lancement dyal l'ecosysteme complet..."
                    sh "docker compose -f docker-compose.yml up -d --build"
                }
            }
        }

        stage('🛡️ Risk Management (Clean Up)') {
            steps {
                script {
                    echo "=> [ÉTAPE 3] Nettoyage dyal les vieilles images Docker..."
                    sh "docker image prune -f"
                }
            }
        }
    }

    post {
        success {
            echo "✅ DÉPLOIEMENT RÉUSSI ABRO!"
            echo "🌐 Frontend: http://10.10.10.50:3005 | ⚙️ Backend: http://10.10.10.50:8084"
        }
        failure {
            echo "❌ ÉCHEC DU DÉPLOIEMENT. Dkhol l'logs dyal Jenkins t-vérifier."
        }
    }
}