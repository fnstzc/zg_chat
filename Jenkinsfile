#!/usr/bin/env groovy

node {
    def branchname = env.BRANCH_NAME
    def buildtag = env.BUILD_TAG
    def app = "zgchat"
    def giturl = "git@develop.kuaiya.cn:nj/zg_chat.git"
    def buildcmd = "/usr/local/software/maven/bin/mvn clean package"
    def dockertag="registry.cn-shenzhen.aliyuncs.com/kynj/${app}-${branchname}:v2-${buildtag}"
    def appname="${app}-${branchname}"

    def k8snamespace = "nanjing"
    def kubectlpath = "/usr/local/software/kubernetes/client/bin/kubectl"

    def kubeconfig = "unknown"
    if(branchname == "staging") {
        kubeconfig="~/.kube/kubeconfig-aliyun-staging"
    } else if(branchname == "master") {
        kubeconfig="~/.kube/kubeconfig-aliyun-prod"
    }

    def mailrecipients = "server_nj@dewmobile.net"

    properties([buildDiscarder(logRotator(numToKeepStr: '10'))])

    try {
        stage('Build') {
            git url: giturl, branch:branchname
            sh "git submodule init"
            sh "git submodule sync"
            sh "git submodule update"
            sh "${buildcmd}"
        }

        stage('Generate docker image') {
            sh """
                #!/usr/bin/env bash
                if [ -d "target/classes" ]; then
                  docker build -t ${dockertag} .
                  docker push ${dockertag}
                fi
            """
        }

        stage('Deploy service') {
            sh """
                #!/usr/bin/env bash
                echo "Deploy ${appname} to aliyun k8s..."
                ${kubectlpath} set image deployment/${appname} ${appname}=${dockertag} -n ${k8snamespace} --kubeconfig ${kubeconfig}
            """
        }
        currentBuild.result = 'SUCCESS'
    } catch (any) {
        currentBuild.result = 'FAILURE'
        throw any
    } finally {
        step([$class:'Mailer', notifyEveryUnstableBuild:true, recipients:mailrecipients, sendToIndividuals:true])
    }
}
