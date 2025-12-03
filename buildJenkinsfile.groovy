pipeline {
    agent any

    parameters {
        string(name: 'branchName', defaultValue: 'main', description: 'Branch name to build from')
        string(name: 'serviceName', defaultValue: 'BookmarkService', description: 'Folder name for service from repo')
    }
    
    tools {
        jdk 'jdk21'  
    }
    
    // environment {
    //     // Явно устанавливаем переменные окружения
    //     JAVA_HOME = "$JAVA_HOME"
    //     PATH = ''
    //     NEXUS_URL = ''
    //     NEXUS_CREDENTIALS_ID = ''
    // }

    stages {
        stage('Environment Check') {
            steps {
                sh """
                    echo "Java version:"
                    java -version
                    echo "Maven version:"
                    mvn -version
                    echo "Current directory:"
                    pwd
                    echo "JAVA_HOME: ${JAVA_HOME}"
                """
            }
        }

        stage('git clone') {
            steps {
                git branch: params.branchName, url: 'https://github.com/spring-projects/spring-petclinic.git'
            }
        }

        stage('Build service') {
            steps {
                sh "mvn -B -DskipTests clean package"
            }
        }


        stage('SonarQube Analysis') {
            steps {
                print('SonarQube Analysis stage here')
            }
        }
        
        stage('Run test') {
            steps {
                sh "mvn test"
            }
        }
       
        stage('Generate Allure report') {
            steps {
                allure includeProperties: false, jdk: '', resultPolicy: 'LEAVE_AS_IS', results: [[path: 'target/allure-results']]
            }
        }
        
        stage('Deploy to Nexus') {
            steps {
                print('Deploy to Nexus stage here')
            }
        }
    }

    post {
        always {
            script {
                sh "ls"
                deleteDir()
                sh "ls"
            }
        }
    }
}