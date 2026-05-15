package com.katallo.dto.storeuser;

import com.katallo.domain.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreUserResponse {

    private Long id;
    private Long userId;
    private Long storeId;

    private String name;
    private String email;

    private Role role;
    private LocalDateTime createdAt;
}