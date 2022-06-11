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