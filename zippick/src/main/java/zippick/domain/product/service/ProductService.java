package zippick.domain.product.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import zippick.domain.product.dto.ProductDto;
import zippick.domain.product.dto.ProductLikedDto;
import zippick.domain.product.dto.response.ProductDetailResponse;
import zippick.domain.product.dto.response.ProductResponse;

public interface ProductService {
    ProductResponse getProductsByKeyword(String keyword, String sort, Long offset);

    ProductResponse getProductsBySize(String category, Long width, Long depth, Long height, String sort, Long offset);

    String compose(MultipartFile roomImage, String furnitureImageUrl, String category);

    ProductDetailResponse getProductDetailById(Long id);

    List<ProductLikedDto> getProductsByIds(List<Long> ids);
}