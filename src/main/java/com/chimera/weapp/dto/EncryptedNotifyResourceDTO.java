package com.chimera.weapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncryptedNotifyResourceDTO {
    private String original_type;
    private String algorithm;
    private String ciphertext;
    private String associated_data;
    private String nonce;
}
