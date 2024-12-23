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


@Document(collection = "activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {
    @Id
    private ObjectId id;
    @Schema(description = "活动名")
    private String title;
    @Schema(description = "首页/我的，竖样式活动图片URL")
    private String imgURL;
    @Schema(description = "菜单，横样式活动图片URL")
    private String imgURL_menu;
    @Schema(description = "活动介绍")
    private String describe;
    @Schema(description = "活动开始时间")
    private Date startTime;
    @Schema(description = "活动结束时间，到时自动下架")
    private Date endTime;
    @Schema(description = "0为下架，1为上架。=0时不返回给小程序")
    private int status;
    @Schema(description = "伪删除，=1时后端不返回")
    private int delete;
}

