package zippick.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    private long id;
    private String name;
    private Long price;
    private String mainImageUrl;
    private Long width;
    private Long depth;
    private Long height;
    private String color;
    private String style;
    private String category;
    private String detailImage;
}
