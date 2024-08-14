# Installation


1. resources 에 secrets.yml 파일 추가 필요 


2. 서버에서 실행시 
```
gradle bootRun --args='--spring.profiles.active=dev' 
혹은

java -jar your-application.jar --spring.profiles.active=dev 
으로 실행
```
3인스턴스에 logstash 설치후 
logstash.conf 확인 필요
sudo vi /etc/logstash/conf.d/logstash.conf

```

input {
    file {
        path => "/home/ec2-user/product-logs/application-*.log"
        start_position => "beginning"
        codec => "json"

    }
}

filter {
    json {
        source => "message"
        remove_field => "message"
    }

}

output {
        elasticsearch {
                hosts => "null"
                user => "null"
                password => "null"
                index => "null"
        }

        stdout {
          id => "logstash"
          codec => rubydebug
        }
}

```


5. api test

   | 메서드 | URL                               | 설명             |
   |-----|-----------------------------------|----------------|
   | GET | /v1/api/products                  | 전체 위스키정보를 조회   |
   | GET | /v1/api/products/{id}             | 특정 위스키정보를 조회   |
   | GET | /v1/api/products?search={keyword} | 키워드로 위스키목록을 조회 |
   | GET | /v1/api/products/top5Keywords     | 인기검색어 TOP5 조회  |
   | GET | /v1/api/products/top5Products     | 인기삼풍 TOP5 조회   |





