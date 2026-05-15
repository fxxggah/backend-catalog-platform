package com.katallo.dto.storeinvite;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreInviteCreateResponse {

    private Long id;
    private String email;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
}