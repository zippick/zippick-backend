package zippick.domain.product.service;

import org.springframework.web.multipart.MultipartFile;
import zippick.domain.product.dto.response.ProductResponse;

public interface ProductService {
    ProductResponse getProductsByKeyword(String keyword, String sort, Long offset);

    ProductResponse getProductsBySize(String category, Long width, Long depth, Long height, String sort, Long offset);

    String compose(MultipartFile roomImage, String furnitureImageUrl, String category);

}