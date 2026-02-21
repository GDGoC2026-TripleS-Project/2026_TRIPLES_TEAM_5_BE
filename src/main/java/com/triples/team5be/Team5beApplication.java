package com.triples.team5be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Team5beApplication {

	public static void main(String[] args) {
		SpringApplication.run(Team5beApplication.class, args);
	}

}
