pipeline {
    agent any
    
    tools {
        jdk 'jdk17'  
    }
    
    environment {
        // Явно устанавливаем переменные окружения
        JAVA_HOME = ''
        PATH = ''
        NEXUS_URL = ''
        NEXUS_CREDENTIALS_ID = ''
    }

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
                git branch: 'main', url: 'https://github.com/Aduscias/AllureExample.git'
            }
        }


        stage('SonarQube Analysis') {

        }
        
        stage('Run test') {
           
        }
       
        stage('Generate Allure report') {
            steps {

            }
        }
        
        stage('Deploy to Nexus') {
            
        }
    
        stage('Run Job') {
            steps {
               
            }
        }
    }
}