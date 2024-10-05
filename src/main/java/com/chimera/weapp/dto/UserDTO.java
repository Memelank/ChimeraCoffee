package com.chimera.weapp.dto;

import com.chimera.weapp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String id;
    private String openid;
    private String name;
    private String school;
    private String role;

    public static UserDTO.UserDTOBuilder ofUser(User user) {
        return UserDTO.builder()
                .id(user.getId().toHexString())
                .openid(user.getOpenid())
                .name(user.getName())
                .school(user.getSchool())
                .role(user.getRole());
    }
}
