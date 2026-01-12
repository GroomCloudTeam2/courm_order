stage('Build') {
    steps {
        sh 'chmod +x gradlew'
        // 여기서 빌드를 먼저 해서 build/libs/ 하위에 jar를 만들어야 함
        sh './gradlew clean build -x test'
    }
}
stage('Docker Build') {
    steps {
        // 이미 만들어진 jar를 Dockerfile이 COPY함
        sh "docker build -t ${IMAGE_NAME} ."
    }
}
