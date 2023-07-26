package com.v1.junopoker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class JunopokerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JunopokerApplication.class, args);
	}

}
