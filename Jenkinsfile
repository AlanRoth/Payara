#!groovy
// Jenkinsfile for building a PR and running a subset of tests against it
def pom
def DOMAIN_NAME
def payaraBuildNumber
pipeline {
    agent any
    options {
        disableConcurrentBuilds()
    }
    environment {
        Dibbles = "\${Dabbles}"
        Bibbly = "Bibbles"
        MP_METRICS_TAGS='tier=integration'
        MP_CONFIG_CACHE_DURATION=0
    }
    tools {
        jdk "zulu-8"
    }
    stages {
        stage('Report') {
            steps {
                script{
                    pom = readMavenPom file: 'pom.xml'
                    payaraBuildNumber = "PR${env.ghprbPullId}#${currentBuild.number}"
                    DOMAIN_NAME = "test-domain"
                    echo "Payara pom version is ${pom.version}"
                    echo "Build number is ${payaraBuildNumber}"
                    echo "Domain name is ${DOMAIN_NAME}"
                }
            }
        }
        stage('Build') {
            steps {
                echo '*#*#*#*#*#*#*#*#*#*#*#*#  Building SRC  *#*#*#*#*#*#*#*#*#*#*#*#*#*#*#'
                withCredentials([usernameColonPassword(credentialsId: 'JenkinsNexusUser', variable: 'NEXUS_USER')]) {
                    sh """mvn -B -V -ff -e clean install -PQuickBuild \
                    -Djavax.net.ssl.trustStore=${env.JAVA_HOME}/jre/lib/security/cacerts \
                    -Djavax.xml.accessExternalSchema=all -Dbuild.number=${payaraBuildNumber} \
                    -Dsurefire.rerunFailingTestsCount=2"""
                }
                echo '*#*#*#*#*#*#*#*#*#*#*#*#    Built SRC   *#*#*#*#*#*#*#*#*#*#*#*#*#*#*#'
            }
            post{
                success{
                    archiveArtifacts artifacts: 'appserver/distributions/payara/target/payara.zip', fingerprint: true
                    archiveArtifacts artifacts: 'appserver/extras/payara-micro/payara-micro-distribution/target/payara-micro.jar', fingerprint: true
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'appserver/distributions/payara/target/stage/payara5/glassfish/logs/server.log'
                }
            }
        }
        stage('Checkout MP TCK Runners') {
            steps{
                echo '*#*#*#*#*#*#*#*#*#*#*#*#  Checking out MP TCK Runners  *#*#*#*#*#*#*#*#*#*#*#*#*#*#*#'
                checkout changelog: false, poll: false, scm: [$class: 'GitSCM',
                    branches: [[name: "*/master"]],
                    userRemoteConfigs: [[url: "https://github.com/payara/MicroProfile-TCK-Runners.git"]]]
                echo '*#*#*#*#*#*#*#*#*#*#*#*#  Checked out MP TCK Runners  *#*#*#*#*#*#*#*#*#*#*#*#*#*#*#'
            }
        }
        stage('Setup for MP TCK Runners') {
            steps {
                setupDomain()
            }
        }
        stage('Run MP TCK Tests') {
            steps {
                echo '*#*#*#*#*#*#*#*#*#*#*#*#  Running test  *#*#*#*#*#*#*#*#*#*#*#*#*#*#*#'
                sh """mvn -B -V -ff -e clean verify \
                -Djavax.net.ssl.trustStore=${env.JAVA_HOME}/jre/lib/security/cacerts \
                -Djavax.xml.accessExternalSchema=all -Dpayara.version=${pom.version} \
                -Dpayara_domain=${DOMAIN_NAME} -Duse.cnHost=true \
                -Dsurefire.rerunFailingTestsCount=2 -Ppayara-server-remote,payara5"""
                echo '*#*#*#*#*#*#*#*#*#*#*#*#  Ran test  *#*#*#*#*#*#*#*#*#*#*#*#*#*#*#'
            }
            post {
                always {
                    zip archive: true, dir: "appserver/distributions/payara/target/stage/payara5/glassfish/domains/${DOMAIN_NAME}/logs", glob: 'server.*', zipFile: 'mp-tck-log.zip'
                    teardownDomain()
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
    }
}

void setupDomain() {
    echo '*#*#*#*#*#*#*#*#*#*#*#*#  Setting up tests  *#*#*#*#*#*#*#*#*#*#*#*#*#*#*#'
    script{
        ASADMIN = "./appserver/distributions/payara/target/stage/payara5/bin/asadmin"
        DOMAIN_NAME = "test-domain"
    }
    sh "${ASADMIN} create-domain --nopassword ${DOMAIN_NAME}"
    sh "${ASADMIN} start-domain ${DOMAIN_NAME}"
    sh "${ASADMIN} start-database || true"
}

void teardownDomain() {
    echo 'tidying up after tests:'
    sh "${ASADMIN} stop-domain ${DOMAIN_NAME}"
    sh "${ASADMIN} stop-database || true"
    sh "${ASADMIN} delete-domain ${DOMAIN_NAME}"
}
