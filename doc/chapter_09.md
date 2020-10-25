# Travis CI, AWS S3, CodeDeploy 연동

## CI/CD
* CI: Continuous Integration - 지속적 통합
* CD: Continuous Deployment - 지속적 배포

형상관리 시스템(VCS)에 소스를 배포하면 자동으로 테스트와 빌드가 수행되어 안정적인 배포 파일을 만들어(CI) 이 빌드 결과를 자동으로 운영 서버에 무중단 배포(CD)까지 진행되는 프로세스 환경을 CI/CD 환경으로 부른다.

CI/CD 환경은 따로 구축되기도 하지만, 함께 구축되는 경우가 많다. 이러한 지속적인 통합/배포 환경이 중요한 이유는 다음과 같다.
* 개발 생산성 저하 방지
  * 여러 개발자가 각자 개발한 소스를 합치는 병합, 통합테스트, 배포 과정들을 모두 수동으로 진행하던 것을 자동화
* 언제든지 특정 버전으로 롤백 가능
* 수십 또는 수백 대의 많은 서버에 한번에 배포
* 자동화된 테스트

이 외에도 다른 이유가 있을 순 있겠지만 기본적으로 프로그램 개발 이외의 일로 개발자의 생산성이 저하되며 프로그램 업데이트 주기가 길어지는 점이 제일 중요한 이유이다.
마틴 파울러는 CI에 대해 4가지 규칙을 지켜야 한다고 말한다. [(참고: 마틴파울러 블로)](https://www.martinfowler.com/articles/originalContinuousIntegration.html)
* 모든 소스 코드가 살아있고 누구든 현재의 소스에 접근할 수 있는 단일 지점을 유지할 것
* 빌드 프로세스를 자동화해서 누구든 소스로부터 시스템을 빌드하는 단일 명령어를 사용할 수 있게 할 것
* 테스팅을 자동화해서 단일 명령어로 언제든지 시스템에 대한 건전한 테스트 숱트를 실행할 수 있게 할 것
* 누구나 현재 실행 파일을 얻으면 지금까지 가장 완전한 실행 파일을 얻었다는 확신을 하게 할 것

## AWS 설정

### IAM 설정
* AWS IAM 사용자 생성하며 다음 권한 설정 (S3 버킷 사용 목적)
  * AmazonS3FullAccess
  * AWSCodeDeployFullAccess
* 생성한 IAM의 AccessKey ID, Secret Key는 안전하게 보관
* AWS 역할 생성하여 다음의 권한 설정 (AWS CodeDeploy 사용 목적)
  * AmazonEC2RoleforAWSCodeDeploy
 
#### IAM의 사용자와 역할의 차이
* 사용자
    * AWS 서비스 외에 사용할 수 있는 권한
    * 로컬PC, IDC 서버 등
* 역할
    * AWS 서비스에서만 할당할 수 있는 권한
    * EC2, CodeDeploy, SQS 등

### S3 버킷 생성
* spring-aws-study-build 이름으로 설정하며 그외의 설정은 기본값으로 진행
* 퍼블릭 액세스 차단(버킷 설정) 항목에서 모든 퍼블릭 엑세스 차단이 활성화되어있는 것이 기본 값임
* 우리가 사용하는 S3는 코드 배포를 위한 패키지 파일이 저장될 저장소이므로 퍼블릭 액세스를 차단하자
* IAM 사용자로 접근이 가능하다
* 이 외에 이미지 등 리소스 저장소로 활용될 경우에는 퍼블릭 액세스를 허용하기도 한다.

### EC2에 IAM 역할 연결
* 인스턴스 설정 > IAM 역할 연결/바꾸기
* CodeDeploy 용 IAM 역할 연결
* 인스턴스 재부팅

## Travis CI 설정
* Travis CI는 Github에서 제공하는 무료 CI 서비스
* https://travis-ci.org 접속
* Github 계정으로 회원가입
* 설정(Settings)에서 CI 구축할 대상 레포지토리(저장소) 활성화
* 환경 변수에 AWS_ACCESS_KEY, AWS_SECRET_KEY 세팅 (위에서 생성항 IAM 키)

### 프로젝트 yml 설정
* Travis CI는 프로젝트 루트에 .travis.yml 파일에 설정한다.
```yml
language: java
jdk:
  - openjdk13

branches:
  only:
    - master

# Travis CI 서버의 Home
## 그레이들을 통해 의존성 라이브러리를 받은 후 해당 디렉토리에 캐시한다.
cache:
  directories:
    - '$HOME/.m2/repository'
    - '$HOME/.gradle'

script: "./gradlew clean build"

# CI 실행 완료시 메일로 알람
notifications:
  email:
    recipients:
      - xxx@gmail.com

# deploy 단계 전 명령으로 AWS CodeDeploy는 JAR를 인식 못하므로 jar + 설정 파일을 zip으로 압
before_deploy:
  - zip -r spring-boot-aws-study *
  - mkdir -p deploy
  - mv spring-boot-aws-study.zip deploy/spring-boot-aws-study.zip

deploy:
  - provider: s3 # AWS S3 설정
    access_key_id: $AWS_ACCESS_KEY # travis environment variables(환경변수)에 설정한 S3 AWS_ACCESS_KEY
    secret_access_key: $AWS_SECRET_KEY # s3 AWS_SECRET_KEY
    bucket: spring-aws-study-build
    region: ap-northeast-2
    skip_cleanup: true
    acl: private
    local_dir: deploy # before_deploy에서 생성한 디렉토리
    wait-until-deployed: true
```


## CodeDeploy 설정

### EC2에 CodeDeploy 에이전트 설치
```bash
# 에이전트 설치
$ aws s3 cp s3://aws-codedeploy-ap-northeast-2/latest/install . --region ap-northeast-2

# 성공 메시지
download: s3://aws-codedeploy-ap-northeast-2/latest/install to ./install

# install파일에 실행 권한 추가
$ chmod +x ./install

# install 진행
$ sudo ./install auto

# 설치 완료 후 에이전트 상태 검사
$ sudo service codedeploy-agent status

# 정상 상태 메시지 출력 확인
The AWS CodeDeploy agent is running as PID xxxgo
```


