package com.chimera.weapp.dto;

import com.chimera.weapp.entity.User;
import com.chimera.weapp.vo.CouponIns;
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
    private String school;
    private String role;
    private int points;
    private List<CouponIns> coupons;

    public static UserDTO.UserDTOBuilder ofUser(User user) {
        return UserDTO.builder()
                .id(user.getId().toHexString())
                .openid(user.getOpenid())
                .name(user.getName())
                .school(user.getSchool())
                .points(user.getPoints())
                .coupons(user.getCoupons())
                .role(user.getRole());
    }
}
