package com.chimera.weapp.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "appConfigurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "配置document，给一些变化频繁的配置用")
public class AppConfiguration {
    @Id
    @NotNull
    private ObjectId id;
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
