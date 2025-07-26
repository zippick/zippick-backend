package zippick.domain.product.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiComposeRequest {
    private MultipartFile roomImage;
    private String furnitureImageUrl;
    private String category;
}
