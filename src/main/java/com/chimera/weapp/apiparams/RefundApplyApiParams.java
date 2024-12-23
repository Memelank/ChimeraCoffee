package com.chimera.weapp.apiparams;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundApplyApiParams {
    @NotNull
    private String orderId;
    @NotBlank
    @Schema(description = "退款原因")
    private String reason;
}
