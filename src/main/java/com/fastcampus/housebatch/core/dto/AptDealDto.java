package com.fastcampus.housebatch.core.dto;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.Getter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 아파트 실거래가 각각의 정보를 담는 객체 ( from XML )
 */
@ToString
@Getter
@XmlRootElement(name = "item") // jaxb 라이브러리 통해 parsing 수월하게 해줌
public class AptDealDto {
    // XML API 각 요소 맵핑
    @XmlElement(name = "거래금액")
    private String dealAmount;

    @XmlElement(name = "건축년도")
    private Integer builtYear;

    @XmlElement(name = "년")
    private Integer year;

    @XmlElement(name = "법정동")
    private String dong;

    @XmlElement(name = "아파트")
    private String aptName;

    @XmlElement(name = "월")
    private Integer month;

    @XmlElement(name = "일")
    private Integer day;

    @XmlElement(name = "전용면적")
    private Double exclusiveArea;

    @XmlElement(name = "지번")
    private String jibun;

    //지번 태그 자체가 없는 경우에 대해 처리
    public String getJibun() {
        return Optional.ofNullable(jibun).orElse("");
    }

    @XmlElement(name = "지역코드")
    private String regionalCode;

    @XmlElement(name = "층")
    private Integer floor;

    @XmlElement(name = "해제사유발생일")
    private String dealCanceledDate; // 21.07.09

    @XmlElement(name = "해제여부")
    private String dealCanceled; // O

    public LocalDate getDealDate() { // aptDeal에서 호출하
        return LocalDate.of(year, month, day);
    }

    public Long getDealAmount() {
        return Long.parseLong(dealAmount.replaceAll(",", "").trim());
    }

    public boolean isDealCanceled() {
        return "O".equals(dealCanceled.trim());
    }

    public LocalDate getDealCanceledDate() {
        if (StringUtils.isBlank(dealCanceledDate)) {
            return null;
        }
        return LocalDate.parse(dealCanceledDate.trim(), DateTimeFormatter.ofPattern("yy.MM.dd"));
    }
}
