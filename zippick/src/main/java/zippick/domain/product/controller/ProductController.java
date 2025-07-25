package zippick.domain.product.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import zippick.domain.product.dto.response.AiComposeResponse;
import zippick.domain.product.dto.response.ProductResponse;
import zippick.domain.product.service.ProductService;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ProductResponse> getProducts(
            @RequestParam(value = "keyword",  required = false) String keyword,
            @RequestParam(value = "min_price",  required = false) Long min_price,
            @RequestParam(value = "max_price",  required = false) Long max_price,
            @RequestParam(value = "width",  required = false) Long width,
            @RequestParam(value = "depth",  required = false) Long depth,
            @RequestParam(value = "height",  required = false) Long height,
            @RequestParam(value = "sort", defaultValue = "latest") String sort,
            @RequestParam(value = "offset", defaultValue = "0") Long offset
    ) {
        ProductResponse response = null;

        if (keyword != null){
            // 키워드 기반 필터링
            response = productService.getProductsByKeyword(
                    keyword,
                    sort,
                    offset
            );
        }

        else if (width != null || depth != null || height != null) {
            // 사이즈 기반 필터링
            response = productService.getProductsBySize(width, depth, height, sort, offset);
        }

         return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/ai-compose", consumes = "multipart/form-data")
    public ResponseEntity<AiComposeResponse> composeImage(
            @Parameter(description = "방 이미지 파일", required = true, schema = @Schema(type = "string", format = "binary"))
            @RequestPart("roomImage") MultipartFile roomImage,

            @Parameter(description = "가구 이미지 URL", required = true)
            @RequestPart("furnitureImageUrl") String furnitureImageUrl,

            @Parameter(description = "카테고리", required = true)
            @RequestPart("category") String category
    ) {
        String resultImageUrl = productService.compose(roomImage, furnitureImageUrl, category);
        return ResponseEntity.ok(new AiComposeResponse(resultImageUrl));
    }

}
