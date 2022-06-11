package com.fastcampus.housebatch.core.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)  // config 클래스의 @EnableJpaAuditing 필요
@NoArgsConstructor
@Table(name = "lawd")
public class Lawd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long lawdId;

    @Column(nullable = false)
    private String lawdCd;

    @Column(nullable = false)
    private String lawdDong;

    @Column(nullable = false)
    private Boolean exist;

    @CreatedDate  // jpa에서 생성시 날짜 넣어줌 (물론 위에 설정 필요)
    private LocalDateTime createdAt;

    @LastModifiedDate  // jpa에서 수정시 날짜 넣어줌
    private LocalDateTime updatedAd;

}
