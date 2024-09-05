package com.chimera.weapp.entity;

import com.chimera.weapp.vo.OptionValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "product_option")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOption {
    @Id
    private ObjectId id;
    private String name;
    private List<OptionValue> values;
}
