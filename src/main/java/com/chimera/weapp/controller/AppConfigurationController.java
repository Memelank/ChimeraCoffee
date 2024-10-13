package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.entity.AppConfiguration;
import com.chimera.weapp.repository.AppConfigurationRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/appConfiguration")
public class AppConfigurationController {
    @Autowired
    private AppConfigurationRepository repository;

    @GetMapping
    @LoginRequired
    @Operation(description = "获取所有配置")
    public ResponseEntity<List<AppConfiguration>> getAllAppConfiguration() {
        return ResponseEntity.ok(repository.findAll());
    }
    //todo 删改操作
}
