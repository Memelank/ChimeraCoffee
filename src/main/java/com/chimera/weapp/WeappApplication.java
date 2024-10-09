package com.chimera.weapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WeappApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeappApplication.class, args);
	}

}
