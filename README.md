# Installation

1. /.env 파일 추가 및 환경변수 설정
```shell
MYSQL_DATABASE={DB주소}
MYSQL_USER={유저네임}
MYSQL_PASSWORD={패스워드}


```
2. /spring/src/main/resources/application.yml 환경변수 설정(Docker로빌드하지 않을경우만)

3. spring 디렉토리에서 .env를 만들어주시고
```shell
MYSQL_DATABASE=jdbc:mysql://database-1.cvmakeaksuml.ap-northeast-2.rds.amazonaws.com/bottle
MYSQL_USER=카톡방 공유
MYSQL_PASSWORD=카톡방 공유
SPRING_PROFILES_ACTIVE=prod
```
해당 파일 작성 필요 


4. 완료후
```shell
 spring 디렉토리에서
nano load-env.sh
```

```shell
load-env.sh 파일 내용:

Bash

복사

**#!/bin/bash**
export $(grep -v '^#' .env | xargs)
```

chmod +x load-env.sh 실행권한 부여
source ./load-env.sh 환경변수 로드

5. 인스턴스에 logstash 설치후 
logstash.conf 수정 필요
sudo vi /etc/logstash/conf.d/logstash.conf

```shell

input {
        file {
           path => "/log/application.log"
         start_position => "beginning"
         sincedb_path => "/dev/null"
        }
}

output {
        elasticsearch {
                hosts => "http://3.39.169.45:9200"
                user => "카톡방 공유"
                password => "카톡방 공유"
                index => "logstash-apache-access-%{+YYYY.MM.dd}"
        }

        stdout {
          id => "logstash"
          codec => rubydebug
        }
}

```


5. api test

   | 메서드 | URL                                                    | 설명             |
   |-----|--------------------------------------------------------|----------------|
   | GET | /v1/api/products                  | 전체 위스키정보를 조회   |
   | GET | /v1/api/products/{id}             | 특정 위스키정보를 조회   |
   | GET | /v1/api/products?search={keyword} | 키워드로 위스키목록을 조회 |

5. test request api
   http://ec2-3-35-214-155.ap-northeast-2.compute.amazonaws.com/v1/api/products/1
6. kibana dashboard (elastic/elastic)
   http://ec2-54-180-249-141.ap-northeast-2.compute.amazonaws.com:5601/app/kibana#/home
