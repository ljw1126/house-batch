package com.fastcampus.housebatch.core.entity;

import com.fastcampus.housebatch.core.dto.AptDealDto;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "apt")
@EntityListeners(AuditingEntityListener.class)
public class Apt {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO.IDENTITY)
    private Long aptId;

    @Column(nullable = false)
    private String aptName;

    @Column(nullable = false)
    private String jibun;

    @Column(nullable = false)
    private String dong;

    @Column(nullable = false)
    private String guLawdCd;

    @Column(nullable = false)
    private Integer builtYear;

    @CreatedDate
    private LocalDateTime cratedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // id나 createDate, lastMOdi- 는 알아서 만들어주니 생략
    public static Apt of(AptDealDto dto) {
        Apt apt = new Apt();
        apt.setAptName(dto.getAptName().trim()); // 정규화 : 띄어쓰기 제거
        apt.setJibun(dto.getJibun().trim());
        apt.setDong(dto.getDong().trim());
        apt.setGuLawdCd(dto.getRegionalCode().trim());
        apt.setBuiltYear(dto.getBuiltYear());
        return apt;
    }
}
