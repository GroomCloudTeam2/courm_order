pipeline {
	agent any

	environment {
		// 아까 등록한 Credentials ID
		GITHUB_TOKEN_ID = 'github-token'
		// 도메인 서비스 이름
		IMAGE_NAME = 'courm_order'
	}

	stages {
		stage('Build') {
			steps {
				sh 'chmod +x gradlew'
				// 테스트 제외 빌드
				sh './gradlew clean build -x test'
			}
		}

		stage('Docker Build') {
			steps {
				// 이미 만들어진 jar를 Dockerfile이 COPY함
				sh "docker build -t ${IMAGE_NAME} ."
			}
		}

		stage('Deploy') {
			steps {
				echo '배포를 시작합니다...'
				// 기존 컨테이너 중지 및 삭제 (에러 무시)
				sh "docker stop ${IMAGE_NAME} || true"
				sh "docker rm ${IMAGE_NAME} || true"
				// 8081 포트로 실행
				sh "docker run -d --name ${IMAGE_NAME} -p 8081:8080 ${IMAGE_NAME}"
			}
		}
	}
}
