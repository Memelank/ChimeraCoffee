package com.chimera.weapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppRunner implements CommandLineRunner {

	@Autowired
	private TestServiceDev testService;

	@Override
	public void run(String... args) throws Exception {
//		// 调用创建新用户的方法

//		userService.createNewUser();
//		testService.createNewProcessorMap();


	}
}
