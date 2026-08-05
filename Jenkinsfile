pipeline {
  agent any
  stages {
    stage('Backend') { steps { sh './mvnw verify' } }
    stage('Frontend') { steps { dir('frontend') { sh 'npm ci && npm test -- --watch=false && npm run build' } } }
    stage('Containers') { steps { sh 'docker compose config --quiet' } }
  }
}
