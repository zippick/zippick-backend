package zippick.domain.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zippick.domain.product.dto.response.ProductDetailResponse;
import zippick.domain.product.dto.ProductDto;

import java.util.List;

@Mapper
public interface ProductMapper {

    List<ProductDto> findProductsByKeywordAndSort(
            @Param("keyword") String keyword,
            @Param("sort") String sort,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    long countProductsByKeyword(@Param("keyword") String keyword);

    List<ProductDto> findProductsBySize(
            @Param("category") String category,
            @Param("width") Long width,
            @Param("depth") Long depth,
            @Param("height") Long height,
            @Param("sort") String sort,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    long countProductsBySize(
            @Param("category") String category,
            @Param("width") Long width,
            @Param("depth") Long depth,
            @Param("height") Long height
    );

    ProductDetailResponse findProductDetailById(@Param("id") Long id);
}
