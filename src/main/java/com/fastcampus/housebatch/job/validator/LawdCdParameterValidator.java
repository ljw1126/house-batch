package com.fastcampus.housebatch.job.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

public class LawdCdParameterValidator implements JobParametersValidator {

    private static final String LAWD_CD = "lawdCd";

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        String lawdCd = parameters.getString(LAWD_CD);

        if (isNotValid(lawdCd)) {
            throw new JobParametersInvalidException(LAWD_CD + "은 5자리 문자열이여야 합니다.");
        }
    }
/*
    조건문에 들어갔던 내용이 읽기 불편(명시적)
    !StringUtils.hasText(lawdCd) || lawdCd.length() != 5  not이 참.. 읽기 곤란함
    그래서 아래와 같이 두 private method로 리팩토링
*/
    private boolean isNotValid(String lawdCd) {
        return !isValid(lawdCd);
    }

    private boolean isValid(String lawdCd) {
        return StringUtils.hasText(lawdCd) && lawdCd.length() == 5;
    }
}
