package com.chimera.weapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {
    @Id
    private ObjectId id;
    private String title;
    private String imgURL;
    private String describe;
}

