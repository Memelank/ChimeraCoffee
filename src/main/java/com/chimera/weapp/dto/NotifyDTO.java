package com.chimera.weapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifyDTO {
    private String id;
    private String create_time;
    private String event_type;
    private String resource_type;
    private EncryptedNotifyResourceDTO resource;
    private String summary;
}
