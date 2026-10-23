package com.findsomeone.find_someone_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan("com.findsomeone.entity")
@EnableJpaRepositories("com.findsomeone.repository")
@SpringBootApplication(scanBasePackages = "com.findsomeone")
public class FindSomeoneBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FindSomeoneBackendApplication.class, args);
	}

}
