package com.katallo.dto.productimage;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadImageRequest {

    private Long productId;
    private MultipartFile file;

}
