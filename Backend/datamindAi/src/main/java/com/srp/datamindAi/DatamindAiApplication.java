package com.srp.datamindAi;

import com.srp.datamindAi.config.BackendProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BackendProperties.class)
public class DatamindAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatamindAiApplication.class, args);
	}

}
