pipeline {
    agent any

    tools {
        maven 'Maven 3.9.9'
        dockerTool 'Docker'
    }

    options {
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    environment {
        // --- ACR ---
        ACR_LOGIN_SERVER = 'jobly.azurecr.io'
        IMAGE_REPO       = 'jobly/jobs'

        // --- Versioning ---
        VERSION_PREFIX   = '0.1'

        // --- ACI deploy ---
        AZ_RESOURCE_GROUP     = 'jobly-rg'
        AZ_CONTAINER_INSTANCE    = 'jobly-jobs-aci'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/main']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/MartijnvCitteren/Jobly-Jobs']]
                )
                sh 'git rev-parse --short HEAD > .git_short'
            }
        }

        stage('Build + Unit/Integration Tests') {
            steps {
                sh 'mvn -B -U clean verify'
            }
        }

        stage('Create Image Tags') {
            steps {
                script {
                    def gitShort = sh(script: 'cat .git_short', returnStdout: true).trim()
                    env.IMAGE_VERSION = "${env.VERSION_PREFIX}.${env.BUILD_NUMBER}-${gitShort}"
                    env.IMAGE_NAME_VERSIONED = "${env.ACR_LOGIN_SERVER}/${env.IMAGE_REPO}:${env.IMAGE_VERSION}"
                    env.IMAGE_NAME_LATEST = "${env.ACR_LOGIN_SERVER}/${env.IMAGE_REPO}:latest"
                }
                sh 'echo "Building image: $IMAGE_NAME_VERSIONED (and tagging as latest)"'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t "$IMAGE_NAME_VERSIONED" -t "$IMAGE_NAME_LATEST" .'
            }
        }

        stage('ACR Login + Push') {
            steps {
                // Option A (recommended): Use Azure CLI + service principal (credentials type: "Secret text" with JSON)
                //  - Create Jenkins credential: azure-sp (Secret text)
                //  - Value: {"appId":"...","password":"...","tenant":"...","subscription":"..."}
                //  - Grant SP: AcrPush on ACR + contributor on RG (or specific ACI permissions)
                withCredentials([string(credentialsId: 'azure-sp', variable: 'AZ_SP_JSON')]) {
                    script {
                        def appId = sh(script: "python3 -c 'import json,os; print(json.loads(os.environ[\"AZ_SP_JSON\"]) [\"appId\"])'", returnStdout: true).trim()
                        def password = sh(script: "python3 -c 'import json,os; print(json.loads(os.environ[\"AZ_SP_JSON\"]) [\"password\"])'", returnStdout: true).trim()
                        def tenant = sh(script: "python3 -c 'import json,os; print(json.loads(os.environ[\"AZ_SP_JSON\"]) [\"tenant\"])'", returnStdout: true).trim()
                        def subscription = sh(script: "python3 -c 'import json,os; print(json.loads(os.environ[\"AZ_SP_JSON\"]) [\"subscription\"])'", returnStdout: true).trim()

                        sh """
                          set -euo pipefail
                          az logout || true
                          az login --service-principal -u '${appId}' -p '${password}' --tenant '${tenant}'
                          az account set --subscription '${subscription}'

                          az acr login --name '${env.ACR_LOGIN_SERVER.split('\\.')[0]}'

                          docker push '${env.IMAGE_NAME_VERSIONED}'
                          docker push '${env.IMAGE_NAME_LATEST}'
                        """
                    }
                }
            }
        }

        stage('Restart ACI') {
            steps {
                sh 'sleep 10'
                sh """
                  set -euo pipefail
                  az container restart --resource-group '${AZ_RESOURCE_GROUP}' --name '${AZ_CONTAINER_GROUP}'
                """
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
            sh 'docker image rm -f "$IMAGE_NAME_VERSIONED" "$IMAGE_NAME_LATEST" 2>/dev/null || true'
        }
    }
}