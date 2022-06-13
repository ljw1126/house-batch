package com.fastcampus.housebatch.job.apt;

import com.fastcampus.housebatch.adaptor.ApartmentApiResource;
import com.fastcampus.housebatch.core.dto.AptDealDto;
import com.fastcampus.housebatch.core.repository.LawdRepository;
import com.fastcampus.housebatch.job.validator.YearMonthParameterValidator;
import com.fastcampus.housebatch.service.AptDealService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

@Configuration
@AllArgsConstructor
@Slf4j
public class AptDealInsertJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final ApartmentApiResource apartmentApiResource;

    // jobParameter validator : edit configuration에서 설정한 값에 대한 유효성 검사
    // new FilePathParameterValidator() 대신 bean으로 생성해서 해도 되고(매개변수 주입방식), 편한대로 하면됨
    @Bean
    public Job aptDealInsertJob(
                                Step guLawdCdStep,
                                Step aptDealInsertStep
                                //Step contextPrintStep
                                //Step aptDealInsertStep
    ) {
        return jobBuilderFactory.get("aptDealInsertJob")
                .incrementer(new RunIdIncrementer())
                .validator(new YearMonthParameterValidator())
                .start(guLawdCdStep)
                .on("CONTINUABLE").to(aptDealInsertStep).next(guLawdCdStep)
                .from(guLawdCdStep)
                .on("*").end()
                .end()
                .build();
    }

    @JobScope
    @Bean
    public Step guLawdCdStep(Tasklet GuLawdCdTasklet) {
        return stepBuilderFactory.get("guLawdCdStep")
                .tasklet(GuLawdCdTasklet)
                .build();
    }

    @StepScope
    @Bean
    public Tasklet GuLawdCdTasklet(LawdRepository lawdRepository) {
        return new GuLawdTasklet(lawdRepository);
    }

    /**
     2. 실수 point
     .. 이렇게 선언 할 경우 실행은 되지만 guLawdCd가 동일한 값만 출력됨!
     Step은 실행할때 한번만 주입받기 때문에 jobExecutionContext 값이 한번만 받아짐..
     그래서 아래와 같이 변경하면 동일한 값만 계속 호출하는 결과가 됨 (하지 말자)
     public Step contextPrintStep(@Value("#{jobExecutionContext['guLawdCd']}") String guLawdCd) {

     return stepBuilderFactory.get("contextPrintStep")
                 .tasklet((contribution, chunkContext) -> {
                     System.out.println("jobExecutionContext['guLawdCd'] > " + guLawdCd);
                     return RepeatStatus.FINISHED;
                 })
                 .build();
     }
     */
    @JobScope
    @Bean
    public Step contextPrintStep(Tasklet contextPrintTasklet) {
        return stepBuilderFactory.get("contextPrintStep")
                .tasklet(contextPrintTasklet)
                .build();
    }

    // ExecutionContext 값을 출력해주는 용도
    @StepScope
    @Bean
    public Tasklet contextPrintTasklet(
            @Value("#{jobExecutionContext['guLawdCd']}") String guLawdCd
    ) {
        return (contribution, chunkContext) -> {
            System.out.println("jobExecutionContext['guLawdCd'] > " + guLawdCd);
            return RepeatStatus.FINISHED;
        };
    }

    @JobScope
    @Bean
    public Step aptDealInsertStep(
            StaxEventItemReader<AptDealDto> aptDealResourceReader,
            ItemWriter<AptDealDto> aptDealWriter
    ) {
        return stepBuilderFactory.get("aptDealInsertStep")
                .<AptDealDto, AptDealDto>chunk(10)
                .reader(aptDealResourceReader)
                .writer(aptDealWriter)
                .build();
    }
    /*
     #XML 스탭 응답 처리
     edit configuration 에 filePath 설정값 넣어줌 -> @Value("#{jobParameters['filePath']}") String filePath
     resource : 해당 filePath 에 있는 파일을 읽겠다.
     addFragmentRootElements : 각 데이터의 root 가 어딘지 reader 한테도 알려줘야함
     unmarshaller : mapping 처리 클래스 구현 후 넣어줌
    */
    @StepScope
    @Bean
    public StaxEventItemReader<AptDealDto> aptDealResourceReader(
            Jaxb2Marshaller aptDealDtoMarshaller,
            @Value("#{jobParameters['yearMonth']}") String yearMonth,
            @Value("#{jobExecutionContext['guLawdCd']}") String guLawdCd
    ) {
        return new StaxEventItemReaderBuilder<AptDealDto>()
                .name("aptDealResourceReader")
                .resource(apartmentApiResource.getResource(guLawdCd, YearMonth.parse(yearMonth)))
                .addFragmentRootElements("item")
                .unmarshaller(aptDealDtoMarshaller)
                .build();
    }

    // dto 만들어 두면 simple하네
    @StepScope
    @Bean
    public Jaxb2Marshaller aptDealDtoMarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(AptDealDto.class); // 미리 만든 dto 클래스로 설정
        return jaxb2Marshaller;
    }

    // 어노테이션 지우고 private 로 해서 호출사용해도 됨 ( team convention )
    @StepScope
    @Bean
    public ItemWriter<AptDealDto> aptDealWriter(AptDealService aptDealService) {
        return items -> {
            items.forEach(aptDealService::upsert);
            System.out.println("================Writing Completed==============");
        };
    }
}
