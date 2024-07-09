# Installation

1. /.env 파일 추가 및 환경변수 설정
```shell
MYSQL_DATABASE={DB주소}
MYSQL_USER={유저네임}
MYSQL_PASSWORD={패스워드}

OPENSEARCH_HOST={오픈서치엔드포인트}
OPENSEARCH_USER={유저네임}
OPENSEARCH_PASS={패스워드}
```
2. /spring/src/main/resources/application.yml 환경변수 설정(Docker로빌드하지 않을경우만)
```shell
export MYSQL_DATABASE={DB주소}
export MYSQL_USER={유저네임}
export MYSQL_PASSWORD={패스워드}
```
3. docker-composse up 실행
```shell
$ docker-compose up 
```
4. api test

   | 메서드 | URL                                                    | 설명             |
   |-----|--------------------------------------------------------|----------------|
   | GET | http://localhost:8080/v1/api/products                  | 전체 위스키정보를 조회   |
   | GET | http://localhost:8080/v1/api/products/{id}             | 특정 위스키정보를 조회   |
   | GET | http://localhost:8080/v1/api/products?search={keyword} | 키워드로 위스키목록을 조회 |
5. opensearch 대쉬보드에서 로그파일 조회

   https://search-demo-pub-opensearch-in5swn3ijsqc3uwckywol3ogke.ap-northeast-2.es.amazonaws.com/_dashboards