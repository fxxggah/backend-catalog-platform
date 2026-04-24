package com.catalog.dto.storeinvite;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreInviteResponse {

    private Long id;
    private String email;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
}