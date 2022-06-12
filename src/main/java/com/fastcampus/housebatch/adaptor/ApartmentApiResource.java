package com.fastcampus.housebatch.adaptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * 실거래가 API 호출하기 위한 파라미터
 * - serviceKey : API 인증키
 * - LAWD_CD : 구 지역코드 ( 법정동 코드 앞자리 5자리 : 구에 해당 )
 * - DEAL_YMD : 거래 년월 ex.201512
 *
 * YearMonth : framework에서 제공하는 클래스
 * > yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM") 포맷변경
 */
@Slf4j
@Component
public class ApartmentApiResource {

    @Value("${external.apartment-api.path}")
    private String path;

    @Value("${external.apartment-api.service-key}")
    private String serviceKey;

    // 일단 테스트 파마리터는 하드코딩해서 던짐
    public Resource getResource(String lawdCd, YearMonth yearMonth) {
        String url = String.format("%s?serviceKey=%s&LAWD_CD=%s&DEAL_YMD=%s", path, serviceKey, lawdCd,
                yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM")));

        log.info("Resource URL : " + url);

        try {
            return new UrlResource(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Failed to created UrlResource"); // 인자 틀렸을때 많이 쓰는 예외, runtimeException 상속
        }
    }
}
