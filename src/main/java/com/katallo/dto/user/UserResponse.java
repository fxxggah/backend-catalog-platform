package com.katallo.dto.user;

import com.katallo.domain.enums.Provider;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Provider provider;
    private Boolean active;
    private LocalDateTime createdAt;
}