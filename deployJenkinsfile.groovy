pipeline {
    agent any

    parameters {
        string(name: 'nexusAtrifactUrl', defaultValue: '', description: 'Url to get artifact from nexus')
    }

    stages {
        stage('Download from Nexus') {
            environment {
                NEXUS_URL = 'nexus:8081'
                NEXUS_CREDS = credentials('nexus_user_password')
            }
            steps {
                sh "wget --user=${NEXUS_CREDS_USR} --password=${NEXUS_CREDS_PSW} -O distr.jar ${params.nexusAtrifactUrl}"
                sh "ls"
            }
        
        }
    
        stage('Deploy') {
            steps {
                print('deploy stage here')
            }
        }
    }

    post {
        always {
            script {
                deleteDir()
            }
        }
    }
}