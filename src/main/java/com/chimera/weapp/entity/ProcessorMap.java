package com.chimera.weapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "processor_map")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessorMap {
    @Id
    private ObjectId id;
    private String state;
    private String event;
    private String[] customerTypes;
    private String[] scenes;
    private int[] processorIds;
}
