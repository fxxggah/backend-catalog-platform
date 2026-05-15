package com.katallo.dto.storeinvite;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreInviteResponse {

    private Long id;
    private String email;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
}