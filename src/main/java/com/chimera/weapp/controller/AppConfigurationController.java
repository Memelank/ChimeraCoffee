package com.chimera.weapp.controller;

import com.chimera.weapp.annotation.LoginRequired;
import com.chimera.weapp.entity.AppConfiguration;
import com.chimera.weapp.repository.AppConfigurationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appConfiguration")
public class AppConfigurationController {
    @Autowired
    private AppConfigurationRepository repository;

    @GetMapping
    @LoginRequired
    @Operation(description = "获取所有配置")
    public ResponseEntity<List<AppConfiguration>> getAllAppConfigurations() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PutMapping
    @LoginRequired
    @Operation(description = "对单个做修改")
    public ResponseEntity<AppConfiguration> updateConfiguration(@Valid @RequestBody AppConfiguration appConfiguration) {
        AppConfiguration save = repository.save(appConfiguration);
        return ResponseEntity.ok(save);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppConfigurationApiParams {
        @Schema(description = "配置项的键名，例如 \"api.url\"")
        @NotNull
        private String key;
        @Schema(description = "配置项的值，例如 \"https://api.example.com\"")
        @NotNull
        private String value;
        @Schema(description = "配置项的描述")
        private String description;
        @Schema(description = "配置类别（用于分组管理）")
        private String category;
    }

    @PostMapping
    @LoginRequired
    @Operation(description = "新增一个")
    public ResponseEntity<AppConfiguration> addConfiguration(@Valid @RequestBody AppConfigurationApiParams apiParams) {
        AppConfiguration save = repository.save(AppConfiguration.builder()
                .key(apiParams.key)
                .value(apiParams.value)
                .description(apiParams.description)
                .category(apiParams.category).build());
        return ResponseEntity.ok(save);
    }

    @DeleteMapping("/{id}")
    @LoginRequired
    @Operation(description = "删除一个")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable String id) {
        if (!ObjectId.isValid(id)) {
            return ResponseEntity.badRequest().build();
        }

        if (repository.existsById(new ObjectId(id))) {
            repository.deleteById(new ObjectId(id));
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}
