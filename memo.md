1. 공공데이터 포털 API 신청 - 국토교통부_아파트매매 실거래자료
   https://www.data.go.kr/data/15058747/openapi.do

2. 프로젝트 생성
   start.spring.io
```text
- 자바 java
- gradle
- dependencies
  ㄴ lombok
  ㄴ spring configuration Processor
  ㄴ spring batch
  ㄴ spring data jpa
  ㄴ spring data jdbc
  ㄴ mysql driver
  ㄴ h2 database >> build.gradle 수정 :: testRuntimeOnly 'com.h2database:h2'
```

3. 프로젝트 루트 경로에 docker-compose.yml 생성 / 작성 / 실행
```text
version : '3'

services :
mysql :
container_name : mysql_house_batch
image : mysql/mysql-server:5.7
environment :
MYSQL_ROOT_HOST : '%'
MYSQL_USER : 'house'
MYSQL_PASSWORD : 'house'
MYSQL_DATABASE : 'house_batch'
ports :
- "3306:3306"
command :
- "mysqld"
- "--character-set-server=utf8mb4"
- "--collation-server=utf8mb4_unicode_ci"

# docker desktop 실행 후
> docker-compose up -d
```
4. resource/application.ym 생성/작성
5. 인텔리제이 edit configuration
   --spring.profiles.active=local
6. 실행 후 확인
   ※ main 있는 클래스에 annotation 추가해주기**  // spring batch schema 생성 안됨 허허
   @EnableBatchProcessing

---
#### 테이블 설계 
* 동 코드 테이블 설계하기 (lawd 테이블)
   - 동 코드 ID, 동 코드, 동 명, 존폐 여부
     ex. 1111010100, 서울특별시 종로구 청운동, 존재  ( 데이터 예시 )

* 매물 테이블 설계 (apt , apt_deal 테이블)
   - 아파트, 아파트 거래 2가지로 나뉨 
   - 아파트 테이블
      - 아파트 ID, 아파트명, 동, 구 코드, 지번, 건축년도

* 유저 관심 테이블 (apt_notification 테이블)




#### 법정동 코드 분석 
1. https://www.code.go.kr 에서 다운로드 
   1. 법정동 코드 목록 조회 -> 법정동 코드 전체 자료 
2. 파일분석 
   1. 시/도 (2)
   2. 시/군/구 (2)
   3. 읍/면/동 (4)
   4. 리 (2)

#### 법정동 코드 배치 실행 
- edit configuration 수정 후 실행하기 (정상동작 확인)
> --spring.profiles.active=local --spring.batch.job.names=lawdInsertJob -filePath=LAWD_CODE_EXAMPLE.txt

#### API 호출 테스트 형식 
기술 문서 참고하여 필수 파라미터 확인 
> http://openapi.molit.go.kr:8081/OpenAPI_ToolInstallPackage/service/rest/RTMSOBJSvc/getRTMSDataSvcAptTrade?LAWD_CD=11110&DEAL_YMD=201512&serviceKey=서비스키

```text 
// 호출 결과 
<response>
   <header>
      <resultCode>00</resultCode>
      <resultMsg>NORMAL SERVICE.</resultMsg>
   </header>
<body>
   <items>
      <item>
         <거래금액> 60,000</거래금액>
         <거래유형> </거래유형>
         <건축년도>1981</건축년도>
         <년>2015</년>
         <법정동> 당주동</법정동>
         <아파트>롯데미도파광화문빌딩</아파트>
         <월>12</월>
         <일>22</일>
         <전용면적>149.95</전용면적>
         <중개사소재지> </중개사소재지>
         <지번>145</지번>
         <지역코드>11110</지역코드>
         <층>8</층>
         <해제사유발생일> </해제사유발생일>
         <해제여부> </해제여부>
      </item>
      (...이하생략)
   </items>
   <numOfRows>10</numOfRows>
   <pageNo>1</pageNo>
   <totalCount>49</totalCount>
</body>
</response>
```