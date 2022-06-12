package com.fastcampus.housebatch.job.apt;

import com.fastcampus.housebatch.core.dto.AptDealDto;
import com.fastcampus.housebatch.job.validator.FilePathParameterValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
@AllArgsConstructor
@Slf4j
public class AptDealInsertJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;


    // jobParameter validator : edit configuration에서 설정한 값에 대한 유효성 검사
    // new FilePathParameterValidator() 대신 bean으로 생성해서 해도 되고(매개변수 주입방식), 편한대로 하면됨
    @Bean
    public Job aptDealInsertJob(Step aptDealInsertStep) {
        return jobBuilderFactory.get("aptDealInsertJob")
                .incrementer(new RunIdIncrementer())
                .validator(new FilePathParameterValidator())
                .start(aptDealInsertStep)
                .build();
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
     edit configuration 에 filePath 설정값 넣어줌 -> @Value("#{jobParameters['filePath']}") String filePath
     resource : 해당 filePath 에 있는 파일을 읽겠다.
     addFragmentRootElements : 각 데이터의 root가 어딘지 reader한테도 알려줘야함
     unmarshaller : mapping 처리 클래스 구현 후 넣어줌
    */
    @StepScope
    @Bean
    public StaxEventItemReader<AptDealDto> aptDealResourceReader(
            @Value("#{jobParameters['filePath']}") String filePath,
            Jaxb2Marshaller aptDealDtoMarshaller
    ) {
        return new StaxEventItemReaderBuilder<AptDealDto>()
                .name("aptDealResourceReader")
                .resource(new ClassPathResource(filePath))
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
