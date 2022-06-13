package com.fastcampus.housebatch.core.repository;

import com.fastcampus.housebatch.core.entity.Apt;
import com.fastcampus.housebatch.core.entity.AptDeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AptDealRepository extends JpaRepository<AptDeal, Long> {

    // 아파트, 전용면적, 거래일자, 금액, 층수
    Optional<AptDeal> findAptDealByAptAndExclusiveAreaAndDealDateAndDealAmountAndFloor(
            Apt apt, Double exclusiveArea, LocalDate dealDate, Long dealAmount, Integer floor
    );
}
