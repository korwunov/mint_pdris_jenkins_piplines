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
                sh "mvn sonar:sonar -Dsonar.projectKey=Digitalbookmark -Dsonar.host.url=$SONAR_HOST -Dsonar.token=$SONAR_TOKEN -Dsonar.exclusions=**/*.java"
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
        
        stage('Upload to Nexus') {
            environment {
                NEXUS_URL = 'nexus:8081'
                NEXUS_CREDENTIALS_ID = 'nexus_user_password'
            }
            steps {
                script {
                    def jarName = ''
                    if (params.serviceName) == 'BookmarkService' {jarName = 'bookmark-service'}
                    if (params.serviceName) == 'AuthService' {jarName = 'auth-service'}
                    if (params.serviceName) == 'FileService' {jarName = 'file-service'}

                    def artifactVersion = "1.0.${env.BUILD_NUMBER}" // Example versioning
                    nexusArtifactUploader(
                        nexusVersion: 'nexus3',
                        protocol: 'http',
                        nexusUrl: env.NEXUS_URL,
                        groupId: "com.digitalbookmark",
                        version: artifactVersion,
                        credentialsId: env.NEXUS_CREDENTIALS_ID,
                        repository: 'maven-releases',
                        artifacts: [
                            [
                                artifactId: params.serviceName, classifier: '', file: "${params.serviceName}/target/${jarName}-0.0.1-SNAPSHOT.jar", type: 'jar'
                            ]
                        ]
                    )
                }
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