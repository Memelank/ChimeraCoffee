package com.chimera.weapp.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;


@Document(collection = "address")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixDeliveryInfo {
    @Id
    private ObjectId id;
    @Schema(description = "对应User学生认证后的学校")
    private String school;
    @Schema(description = "可选时间")
    private List<String> times;
    @Schema(description = "可选地址")
    private List<String> addresses;
}
