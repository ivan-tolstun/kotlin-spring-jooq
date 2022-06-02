// NOTE:
// * This Jenkinsfile uses functions from: https://git.exceet-secure-solutions.de/development/jenkins-pipeline-global-library
//   specifically those functions are: setupMaven, mvn, gitCheckout, versioningHandler and archiveTestResults for now.
// * Jenkins currently has issues, updating Jenkinsfiles on the first try, for bigger changes or new Jenkinsfiles
//   simply trigger a normal build twice for updating the Build Parameters
pipeline {


    environment {
        JENKINS_MAVEN_SECRETS = credentials('jenkins-maven-settings-security')
    }


    agent {
        docker {
            image 'docker.exceet-secure-solutions.de/ess/maven-java-11-openjdk'
            args '-v /var/run/docker.sock:/var/run/docker.sock -u jenkins'
            registryCredentialsId 'upload-nexus3'
            registryUrl 'https://docker.exceet-secure-solutions.de'
            alwaysPull true
            reuseNode false
        }
    }


    stages {


        //git checkout and workspace cleanup, if you want to always cleanup the git repo use "cleanup = true"
        stage('cleanup and checkout') {
            steps {
                script {
                    gitCheckout {
                        cleanup = true
                        verbose = true
                    }
                }
            }
        }


        stage ("Default-stage") {
            steps {
                script {

                        stage ('INSTALL') {
                            mvn "clean install -Dmaven.test.skip=true"
                        }

                        stage ('TEST') {
                            mvn "test"
                        }

                        stage ('DEPLOY') {
                            mvn "deploy -Dmaven.test.skip=true"
                        }
                }
            }
        }
    }


    post {
        failure {
            emailext (
                subject: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
                <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>""",
                recipientProviders: [[$class: 'CulpritsRecipientProvider']]
                )
        }
        always { deleteDir() }
    }

}