package com.example.ta.ta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.example.ta.repository")
@EntityScan("com.example.ta.model")
@ComponentScan(basePackages = {
	"com.example.ta.controller",
	"com.example.ta.model",
	"com.example.ta.repository",
})
public class TaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaApplication.class, args);
	}

}
