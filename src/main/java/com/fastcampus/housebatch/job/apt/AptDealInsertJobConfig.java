package com.fastcampus.housebatch.job.apt;

import com.fastcampus.housebatch.adaptor.ApartmentApiResource;
import com.fastcampus.housebatch.core.dto.AptDealDto;
import com.fastcampus.housebatch.core.repository.LawdRepository;
import com.fastcampus.housebatch.job.validator.FilePathParameterValidator;
import com.fastcampus.housebatch.job.validator.LawdCdParameterValidator;
import com.fastcampus.housebatch.job.validator.YearMonthParameterValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Configuration
@AllArgsConstructor
@Slf4j
public class AptDealInsertJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final ApartmentApiResource apartmentApiResource;
    private final LawdRepository lawdRepository;

    // jobParameter validator : edit configuration에서 설정한 값에 대한 유효성 검사
    // new FilePathParameterValidator() 대신 bean으로 생성해서 해도 되고(매개변수 주입방식), 편한대로 하면됨
    @Bean
    public Job aptDealInsertJob(//Step aptDealInsertStep,
                                Step guLawdCdStep,
                                Step contextPrintStep) {
        return jobBuilderFactory.get("aptDealInsertJob")
                .incrementer(new RunIdIncrementer())
                //.validator(aptDealJobParametersValidator())
                .start(guLawdCdStep)
                .next(contextPrintStep)
                .build();
    }

    // 2개이상의 Validator 를 composite 해서 지원함 👉 @Override 되어 있는 validate 확인하기
    private JobParametersValidator aptDealJobParametersValidator() {
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        validator.setValidators(Arrays.asList(
            new YearMonthParameterValidator(),
            new LawdCdParameterValidator()
        ));
        return validator;
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
    public Tasklet GuLawdCdTasklet() {
        return (contribution, chunkContext) -> {
            StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
            ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();

            List<String> guLawdCds = lawdRepository.findDistinctGuLawdCd();
            executionContext.putString("guLawdCd", guLawdCds.get(0)); // 0번째에 무조건 있다고 가정(확인용)

            return RepeatStatus.FINISHED;
        };
    }

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
            @Value("#{jobParameters['lawdCd']}") String lawdCd
    ) {
        return new StaxEventItemReaderBuilder<AptDealDto>()
                .name("aptDealResourceReader")
                .resource(apartmentApiResource.getResource(lawdCd, YearMonth.parse(yearMonth)))
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
    public ItemWriter<AptDealDto> aptDealWriter() {
        return items -> {
            items.forEach(System.out::println);
            System.out.println("========= Writing Completed============");
        };
    }
}
