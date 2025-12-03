pipeline {
    agent any

    parameters {
        string(name: 'branchName', defaultValue: 'main', description: 'Branch name to build from')
        string(name: 'serviceName', defaultValue: 'BookmarkService', description: 'Service (module) name')
    }
    
    tools {
        jdk 'jdk17'  
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
                git branch: params.branchName, url: 'https://github.com/korwunov/DigitalBookmark.git'
            }
        }

        stage('Build service') {
            steps {
                sh "mvn -B -DskipTests -pl ${params.serviceName} clean package"
            }
        }


        stage('SonarQube Analysis') {
            environment {
                SONAR_HOST = "http://sonarqube:9000"
                SONAR_TOKEN = credentials('sonarqube_token')
            }
            steps {
                sh "mvn sonar:sonar -Dsonar.projectKey=${params.serviceName} -Dsonar.host.url=$SONAR_HOST -Dsonar.token=$SONAR_TOKEN -Dsonar.exclusions=**/target/**/*"
            }
        }
        
        stage('Run test') {
            steps {
                sh "mvn -pl ${params.serviceName} test"
            }
        }
       
        stage('Generate Allure report') {
            steps {
                allure includeProperties: false, jdk: '', resultPolicy: 'LEAVE_AS_IS', results: [[path: "${params.serviceName}/target/allure-results"]]
            }
        }
        
        stage('Deploy to Nexus') {
            steps {
                print('Deploy to Nexus stage here')
            }
        }
    }

    // post {
    //     always {
    //         script {
    //             sh "ls"
    //             deleteDir()
    //             sh "ls"
    //         }
    //     }
    // }
}