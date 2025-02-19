package com.chimera.weapp.dto;

import com.chimera.weapp.entity.User;
import com.chimera.weapp.vo.CouponIns;
import com.chimera.weapp.vo.DeliveryInfo;
import com.chimera.weapp.vo.PointsProductIns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    @NotNull
    private String id;
    private String openid;
    private String name;
    private Boolean studentCert;
    private String school;
    private String role;
    private String number;
    private int points;
    private List<CouponIns> coupons;
    private List<PointsProductIns> pointsProducts;
    private DeliveryInfo deliveryInfo;

    public static UserDTO.UserDTOBuilder ofUser(User user) {
        return UserDTO.builder()
                .id(user.getId().toHexString())
                .openid(user.getOpenid())
                .name(user.getName())
                .studentCert(user.getStudentCert())
                .school(user.getSchool())
                .points(user.getPoints())
                .coupons(user.getCoupons())
                .number(user.getNumber())
                .pointsProducts(user.getPointsProducts())
                .deliveryInfo(user.getDeliveryInfo())
                .role(user.getRole());
    }
}
