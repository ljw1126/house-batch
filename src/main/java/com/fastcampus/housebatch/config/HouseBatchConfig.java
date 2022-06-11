package com.fastcampus.housebatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing  // 어노테이션으로 entity 데이터를 생성가능
public class HouseBatchConfig {
}
