package zippick.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductLikedDto {
    private Long id;
    private String name;
    private Long price;
    private String imageUrl;
}
