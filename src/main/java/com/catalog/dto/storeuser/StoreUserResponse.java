package com.catalog.dto.storeuser;

import com.catalog.domain.enums.Role;
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
    private String pictureUrl;

    private Role role;
    private LocalDateTime createdAt;
}