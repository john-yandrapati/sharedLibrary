package groovy

class gitCheckout {

    void checkoutGit(Map stageParams) {
    
        checkout([
            $class: 'GitSCM',
            branches: [[name:  stageParams.branch ]],
            userRemoteConfigs: [[ url: stageParams.url ]]
        ])
    }
}
