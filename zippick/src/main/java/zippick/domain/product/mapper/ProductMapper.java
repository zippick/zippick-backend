package zippick.domain.product.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zippick.domain.product.dto.ProductDto;
import zippick.domain.product.dto.ProductLikedDto;
import zippick.domain.product.dto.response.ProductDetailResponse;

@Mapper
public interface ProductMapper {

    List<ProductDto> findProductsByKeywordAndCategoryAndSort(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("sort") String sort,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    long countProductsByKeywordAndCategory(
            @Param("keyword") String keyword,
            @Param("category") String category
    );


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

    List<ProductLikedDto> findProductsByIds(@Param("ids") List<Long> ids);

    List<ProductDto> findProductsByCategoryAndPrice(
            @Param("category") String category,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            @Param("sort") String sort,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    long countProductsByCategoryAndPrice(
            @Param("category") String category,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice
    );

    List<ProductLikedDto> findByCategoryAndTone(
            @Param("category") String category,
            @Param("toneCategories") List<String> toneCategories
    );

    List<ProductLikedDto> findByCategoryAndTags(
            @Param("category")String category,
            @Param("tags") List<String> tags
    );
}
