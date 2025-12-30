pipeline {
	agent any

	tools {
		maven 'Maven 3.9.9'
		jdk 'JDK21'
		git 'DefaultGit'
	}

	options {
		timestamps()
		disableConcurrentBuilds()
		timeout(time: 30, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr: '30'))
	}

	environment {
		// --- Versioning ---
		VERSION_PREFIX = '0.1'
	}

	stages {
		stage('Checkout') {
			steps {
				checkout scmGit(
					branches: [[name: '*/main']],
					extensions: [],
					userRemoteConfigs: [[url: 'https://github.com/MartijnvCitteren/Jobly-Jobs.git',
						credentialsId: 'GITHUB_TOKEN']]
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
					withCredentials([
						string(credentialsId: 'ACR_LOGIN_SERVER', variable: 'ACR_LOGIN_SERVER'),
						string(credentialsId: 'IMAGE_REPO', variable:'IMAGE_REPO')]){
						def gitShort = sh(script: 'cat .git_short', returnStdout: true).trim()

						env.IMAGE_VERSION = "${env.VERSION_PREFIX}.${env.BUILD_NUMBER}-${gitShort}"
						env.IMAGE_NAME_VERSIONED = "${env.ACR_LOGIN_SERVER}/${env.IMAGE_REPO}:${env.IMAGE_VERSION}"
						env.IMAGE_NAME_LATEST = "${env.ACR_LOGIN_SERVER}/${env.IMAGE_REPO}:latest"
					}
				}
				sh 'echo "Building image: $IMAGE_NAME_VERSIONED and tagging as latest $IMAGE_NAME_LATEST"'
			}
		}

		stage('Docker Build') {
			steps {
				sh 'docker build -t "$IMAGE_NAME_VERSIONED" -t "$IMAGE_NAME_LATEST" .'
			}
		}

		stage('ACR Login + Push') {
			steps {
				withCredentials([
					usernamePassword(credentialsId: 'ACR_ACCOUNT', usernameVariable: 'AZ_CLIENT_ID', passwordVariable: 'AZ_CLIENT_SECRET'),
					string(credentialsId: 'TENANT_ID', variable: 'AZ_TENANT_ID'),
					string(credentialsId: 'SUBSCRIPTION_ID', variable: 'AZ_SUBSCRIPTION_ID')
				]) {
					sh '''bash -lc '
        set -euo pipefail

        az logout || true

        az login --service-principal \
          -u "$AZ_CLIENT_ID" \
          -p "$AZ_CLIENT_SECRET" \
          --tenant "$AZ_TENANT_ID"

        az account set --subscription "$AZ_SUBSCRIPTION_ID"

        az acr login --name jobly

        docker push "$IMAGE_NAME_VERSIONED"
        docker push "$IMAGE_NAME_LATEST"
      ' '''
				}
			}
		}

	stage('Restart ACI') {
		steps {
			withCredentials([string(credentialsId: 'RESOURCE_GROUP', variable: 'AZ_RESOURCE_GROUP'),
				string(credentialsId: 'CONTAINER_GROUP', variable: 'AZ_CONTAINER_GROUP')
			]){
				sh 'sleep 10'

				sh """
                  set -euo pipefail
                  az container restart --resource-group '${env.AZ_RESOURCE_GROUP}' --name '${env.AZ_CONTAINER_GROUP}'

				        """
			}
		}
	}
}

post {
	always {
		junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
		junit testResults: '**/target/failsafe-reports/*.xml', allowEmptyResults: true
		archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
		sh 'docker image rm -f "$IMAGE_NAME_VERSIONED" "$IMAGE_NAME_LATEST" 2>/dev/null || true'
	}
}
}