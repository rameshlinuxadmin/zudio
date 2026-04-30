def buildCI = (
  env.BRANCH_NAME ==~ /^(main|release\/v\d+(\.\d+)*)$/
) || (
  env.CHANGE_ID && pullRequest?.labels?.contains('CI')
)

pipeline {
  agent any

  stages {
    stage('CI') {
      when {
        expression { buildCI }
      }
      steps {
        echo "Running CI for ${env.BRANCH_NAME}"
      }
    }

    stage('Deploy Prod') {
      when {
        expression { env.BRANCH_NAME.startsWith('release/') }
      }
      steps {
        echo "Deploying to production"
      }
    }
  }
}
