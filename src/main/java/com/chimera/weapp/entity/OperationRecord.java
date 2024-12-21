package com.chimera.weapp.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "operation_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationRecord {
    @Id
    private ObjectId id;
    private ObjectId inventoryId; // 对应的库存物品 ID
    private String operationType; // "入库" 或 "查清出库"
    private int amount; // 入库数量或清查出库数量

    @CreatedDate
    private Date timestamp; // 操作时间
}
