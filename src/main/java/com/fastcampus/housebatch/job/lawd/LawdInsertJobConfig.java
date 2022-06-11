package com.fastcampus.housebatch.job.lawd;

import com.fastcampus.housebatch.core.entity.Lawd;
import com.fastcampus.housebatch.job.validator.FilePathParameterValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static com.fastcampus.housebatch.job.lawd.LawdFieldSetMapper.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class LawdInsertJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job lawdInsertJob(Step lawdInsertStep) {
        return jobBuilderFactory.get("lawdInsertJob")
                .incrementer(new RunIdIncrementer())
                .validator(new FilePathParameterValidator())
                .start(lawdInsertStep)
                .build();
    }

    @JobScope
    @Bean
    public Step lawdInsertStep(FlatFileItemReader<Lawd> lawdFlatFileItemReader,
                               ItemWriter<Lawd> lawdItemWriter) { // 1000개씩 끊어서 처리
        return stepBuilderFactory.get("lawdInsertStep")
                .<Lawd, Lawd>chunk(1000)
                .reader(lawdFlatFileItemReader)
                .writer(lawdItemWriter)
                .build();
    }

    /**
     *
     * delimiter : 파일에서 텍스트 구분자
     * names : 맵핑될 필드
     * resource : 읽어올 파일 소스
     * fieldSetMapper : 인터페이스 FieldSetMapper 구현한 클래스 넣어 줌(맵핑처리하는 클래스)
     * @param filePath
     * @return
     */
    @Bean
    @StepScope
    public FlatFileItemReader<Lawd> lawdFlatFileItemReader(@Value("#{jobParameters['filePath']}")String filePath) {
        return new FlatFileItemReaderBuilder<Lawd>()
                .name("lawdFlatFileItemReader")
                .delimited()
                .delimiter("\t")
                .names(LAWD_CD, LAWD_DONG, EXIST)
                .linesToSkip(1)
                .fieldSetMapper(new LawdFieldSetMapper())
                .resource(new ClassPathResource(filePath))
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<Lawd> lawdItemWriter() {
        return new ItemWriter<Lawd>() {
            @Override
            public void write(List<? extends Lawd> items) throws Exception {
                items.forEach(System.out::println);
            }
        };
    }
}
