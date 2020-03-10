def call(body) {
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        agent {
            docker {
                image 'maven:3-alpine'
                args '-v /root/.m2:/root/.m2'
            }
        }
        stages {
            stage('Build') {
                steps {
                    script {  
                        def data = readYaml file: pipelineParams.yamlFile 
                        sh "${data.mvn.command}"
                    }
                }
            }
            stage('Test') {
                steps {
                    script {  
                        def data = readYaml file: pipelineParams.yamlFile 
                        sh "${data.mvn.test}"
                    }
                }
                post {
                    always {
                        junit 'target/surefire-reports/*.xml'
                    }
                }
            }
            stage('Deliver') {
                steps {
                    script {  
                        def data = readYaml file: pipelineParams.yamlFile 
                        sh "${data.mvn.deliver}"
                    }
                }
            }
        }
    }   
}