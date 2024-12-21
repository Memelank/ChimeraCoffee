package com.chimera.weapp.entity;

import com.chimera.weapp.vo.OptionValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    @Id
    private ObjectId id;
    private String name;
    private String type; // 类型
    private boolean deleted; // 1是删除，0是正常
    private String unit; // 计量单位
    private int remain; // 剩余量
    
}
