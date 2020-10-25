#!/bin/bash

REPOSITORY=/home/ec2-user/app/step2
PROJECT_NAME=spring-boot-aws-study

echo "> Build 파일 복사"

cp $REPOSITORY/zip/*.jar $REPOSITORY

echo "> 현재 구동 중인 애플리케이션 pid 확인"

## 현재 실행 중인 스프링 부트 애플리케이션의 프로세스 ID를 찾는다. 다른 애플리케이션 이름의 프로그램이 있을 수 있으므로 프로세스를 찾은 뒤 ID를 찾는다.
CURRENT_PID=$(pgrep -fl springboot-aws-study-webservice | grep jar | awk '{print $1}')

echo "현재 구동 중인 애플리케이션 pid: $CURRENT_PID"

if [ -z "$CURRENT_PID" ]; then
  echo "> 현재 구동 중인 애플리케이션이 없으므로 종료하지 않습니다."
else
  echo "> kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

echo "> 새 애플리케이션 배포"
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

echo "> JAR Name: $JAR_NAME"
echo "> $JAR_NAME 에 실행권한 추가"
chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"

nohup java -jar \
        -Dspring.config.location=classpath:/application.properties,/home/ec2-user/app/application-oauth.properties,/home/ec-2user/app/application-real-db.properties \
        -Dspring.profiles.active=real \
        $JAR_NAME > $REPOSITORY/$JAR_NAME 2>&1 &

## $JAR_NAME > $REPOSITORY/$JAR_NAME 2>&1 & 중요하다
## nohup 실행시 CodeDeploy가 무한 대기 하므로 CodeDeploy 로그에 표준 입출력이 출력되도록 설정한다.
