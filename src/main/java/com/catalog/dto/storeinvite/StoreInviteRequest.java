package com.catalog.dto.storeinvite;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreInviteRequest {

    @NotNull
    private Long storeId;

    @NotBlank
    @Email
    private String email;
}