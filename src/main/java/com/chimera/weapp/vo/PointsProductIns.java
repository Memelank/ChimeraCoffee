package com.chimera.weapp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsProductIns {
    @NotNull
    private String uuid;
    @Schema(description = "对应User.id")
    private String userId;
    @Schema(description = "对应PointsProduct.id")
    private String pointsProductId;
    @Schema(description = "PointsProduct.name")
    private String name;
    @Schema(description = "领取方式，0为自提，1为填信息邮递")
    private int sendType;
    @Schema(description = "姓名")
    private String Name;
    @Schema(description = "号码")
    private String number;
    @Schema(description = "邮递人地址")
    private String sendAddr;
    @Schema(description = "自提时间")
    private Date getDate;
    @Schema(description = "是否已领取，0为未领取，1为已领取")
    private int received = 0;
}
