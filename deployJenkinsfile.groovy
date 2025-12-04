pipeline {
    agent any

    parameters {
        string(name: 'nexusAtrifactUrl', defaultValue: '', description: 'URL to get artifact from nexus')
    }

    stages {
        stage('Download from Nexus') {
            environment {
                NEXUS_CREDS = credentials('nexus_user_password')
            }
            steps {
                sh "wget --user=${NEXUS_CREDS_USR} --password=${NEXUS_CREDS_PSW} -O distr.jar ${params.nexusAtrifactUrl}"
            }
        
        }
    
        stage('Deploy') {
            steps {
                sshPublisher(publishers: [
                    sshPublisherDesc(
                        configName: 'prod',
                        transfers: [
                            sshTransfer(
                                sourceFiles: 'distr.jar',
                                remoteDirectory: '/home/admin',
                                execCommand: 'nohup java -jar distr.jar &'
                            )
                        ]
                    )
                ])
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