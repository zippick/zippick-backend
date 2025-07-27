package zippick.domain.product.dto.response;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zippick.domain.product.dto.ProductDto;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private List<ProductDto> products;
    private long totalCount;
}
