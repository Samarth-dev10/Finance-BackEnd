package com.finance.FinTech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.finance")
@EntityScan(basePackages = "com.finance")
@EnableJpaRepositories(basePackages = "com.finance")
public class FinTechApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinTechApplication.class, args);
	}

}
