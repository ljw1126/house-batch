package com.fastcampus.housebatch.service;

import com.fastcampus.housebatch.core.entity.Lawd;
import com.fastcampus.housebatch.core.repository.LawdRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class LawdService {

    private final LawdRepository lawdRepository;

    /**
     * 서비스 중에 데이터 날라가는 경우를 고려해서 조금 느리지만 조회 내용 넣음
     * @param lawd
     */
    @Transactional
    public void upsert(Lawd lawd) {
        Lawd saved = lawdRepository.findByLawdCd(lawd.getLawdCd())
                .orElseGet(Lawd::new); // null 일때 새로 생성

        saved.setLawdCd(lawd.getLawdCd());
        saved.setLawdDong(lawd.getLawdDong());
        saved.setExist(lawd.getExist());

        lawdRepository.save(saved);
    }

}
