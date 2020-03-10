def call(body) {
    
   def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        environment {
            dockerImage = ''
        }
        agent any
        tools {nodejs "nodejs"}
        stages {
            stage('Cloning Git') {
                steps { 
                    script {  
                        def data = readYaml file: pipelineParams.yamlFile   
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name:  "${data.branch}" ]],
                            userRemoteConfigs: [[ url: "${data.url}" ]]
                        ])
                    }
                }
            }
            stage('Build') {
                steps {
                    script {
                        def data = readYaml file: pipelineParams.yamlFile   
                        sh "apk add nodejs"
                        sh "${data.node.command}"
                    }
                }
            }
            stage('Test') {
                steps {
                    script {
                        def data = readYaml file: pipelineParams.yamlFile 
                        sh "apk add nodejs"
                        sh "${data.node.test}"
                    }
                }
            }
            stage('Building image') {
                steps{
                    script {
                        def data = readYaml file: pipelineParams.yamlFile 
                        dockerImage = docker.build "${data.docker.dockerRegistry}" + ":$BUILD_NUMBER"
                    }
                }
            }
            stage('Upload Image') {
                steps{
                    script {
                        def data = readYaml file: pipelineParams.yamlFile 
                        docker.withRegistry( '', "${data.docker.dockerRegistryCredential}" ) {
                            dockerImage.push()
                        }
                    }
                }
            }
            stage('Remove Unused docker image') {
                steps{
                    script {
                        def data = readYaml file: pipelineParams.yamlFile 
                        sh "docker rmi \"${data.docker.dockerRegistry}\":$BUILD_NUMBER"
                    }
                }
            }
        }
    }
}