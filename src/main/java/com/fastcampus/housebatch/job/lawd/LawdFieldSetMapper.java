package com.fastcampus.housebatch.job.lawd;

import com.fastcampus.housebatch.core.entity.Lawd;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class LawdFieldSetMapper implements FieldSetMapper {

    public static final String LAWD_CD = "lawdCd";
    public static final String LAWD_DONG = "lawdDong";
    public static final String EXIST = "exist";

    public static final String EXIST_TRUE_VALUE = "존재";

    @Override
    public Object mapFieldSet(FieldSet fieldSet) throws BindException {
        Lawd lawd = new Lawd();

        lawd.setLawdCd(fieldSet.readString(LAWD_CD));
        lawd.setLawdDong(fieldSet.readString(LAWD_DONG));
        lawd.setExist(fieldSet.readBoolean(EXIST, EXIST_TRUE_VALUE)); // 2번째 trueValue와 비교해서 같으면 true, 아니면 false로 읽어 줌

        return lawd;
    }
}
