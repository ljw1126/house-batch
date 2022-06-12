package com.fastcampus.housebatch.job.apt;

import com.fastcampus.housebatch.core.repository.LawdRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;
/**
 * ExecutionContext에 저장할 데이터
 * 1. guLawdCdList : 구 코드 리스트
 * 2. guLawdCd : 구 코드 -> 다음 스텝에서 활용할 값
 * 3. itemCount : 남아있는 구 코드(아이템) 개수
 * @return
 */
@RequiredArgsConstructor          // final 선언된거만 주입
public class GuLawdTasklet implements Tasklet {

    private final LawdRepository lawdRepository;
    private List<String> guLawdCds; // 맴버 변수로 격상
    private int itemCount;

    private static final String KEY_GU_LAWD_CD = "guLawdCd";
    private static final String KEY_ITEM_COUNT = "itemCount";
    private static final String KEY_GU_LAWD_CD_LIST = "guLawdCdList";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ExecutionContext executionContext = getExecutionContext(chunkContext);
        initList(executionContext);
        initItemCount(executionContext);

        if (itemCount == 0) {
            contribution.setExitStatus(ExitStatus.COMPLETED);
            return RepeatStatus.FINISHED;
        }

        itemCount--;
        /*
            1. 실수 point
            chunkContext.getStepContext().getJobExecutionContext().put(KEY_GU_LAWD_CD, guLawdCds.get(itemCount));
            -> 에러 발생
            -> chunkContext.getStepContext().getJobExecutionContext() return 이 unmodifiedMap이라 put 하면 unsupport 에러 뜸

        */
        executionContext.putString(KEY_GU_LAWD_CD, guLawdCds.get(itemCount));
        executionContext.putInt(KEY_ITEM_COUNT, itemCount);

        contribution.setExitStatus(new ExitStatus("CONTINUABLE"));
        return RepeatStatus.FINISHED;
    }

    private ExecutionContext getExecutionContext(ChunkContext chunkContext) {
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        return stepExecution.getJobExecution().getExecutionContext();
    }

    private void initList(ExecutionContext executionContext) {
        if (executionContext.containsKey(KEY_GU_LAWD_CD_LIST)) {
            guLawdCds = (List<String>) executionContext.get(KEY_GU_LAWD_CD_LIST);
        } else {
            guLawdCds = lawdRepository.findDistinctGuLawdCd();
            executionContext.put(KEY_GU_LAWD_CD_LIST, guLawdCds);
            executionContext.putInt(KEY_ITEM_COUNT, guLawdCds.size());
        }
    }

    private void initItemCount(ExecutionContext executionContext) {
        if (executionContext.containsKey(KEY_GU_LAWD_CD_LIST)) {
            itemCount = executionContext.getInt(KEY_ITEM_COUNT);
        } else {
            itemCount = guLawdCds.size();
        }
    }
}
