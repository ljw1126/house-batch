package com.fastcampus.housebatch.job.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 아파트 실거래가 API 동작시 사용하는 유효성검사
 */
public class YearMonthParameterValidator implements JobParametersValidator {

    private static final String YEAR_MONTH = "yearMonth";

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        String yearMonth = parameters.getString(YEAR_MONTH);
        if (!StringUtils.hasText(yearMonth)) {
            throw new JobParametersInvalidException(yearMonth + "가 빈 문자열이거나 존재하지 않습니다.");
        }

        try {
            YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyyMM")); // parse 에러시 DateTimeParseException 던짐
        } catch (DateTimeParseException dateTimeParseException) {
            throw new JobParametersInvalidException(yearMonth + "가 올바른 날짜 형식이 아닙니다. yyyyMM");
        }
    }
}
