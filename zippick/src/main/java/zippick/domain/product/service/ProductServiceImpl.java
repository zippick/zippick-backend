package zippick.domain.product.service;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.json.JSONArray;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zippick.domain.product.dto.response.ProductDto;
import zippick.domain.product.dto.response.ProductResponse;
import zippick.domain.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.json.JSONObject;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import zippick.global.exception.ErrorCode;
import zippick.global.exception.ZippickException;
import zippick.infra.file.s3.S3Uploader;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final S3Uploader s3Uploader;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${replicate.api.token}")
    private String replicateApiToken;


    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductsByKeyword(String keyword, String sort, Long offset) {
        try {
            long limit = 4; // 가져올 개수

            List<ProductDto> products = productMapper.findProductsByKeywordAndSort(keyword, sort, offset, limit);
            long totalCount = productMapper.countProductsByKeyword(keyword);

            return ProductResponse.builder()
                    .products(products)
                    .totalCount(totalCount)
                    .build();
        } catch (Exception e) {
            throw new ZippickException(ErrorCode.INTERNAL_SERVER_ERROR, "키워드로 상품 검색 실패: "+e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductsBySize(String category, Long width, Long depth, Long height, String sort, Long offset) {
        try {
            long limit = 4; // 가져올 개수

            List<ProductDto> products = productMapper.findProductsBySize(category, width, depth, height, sort, offset, limit);
            long totalCount = productMapper.countProductsBySize(category, width, depth, height);

            return ProductResponse.builder()
                    .products(products)
                    .totalCount(totalCount)
                    .build();
        } catch (Exception e) {
            throw new ZippickException(ErrorCode.INTERNAL_SERVER_ERROR, "사이즈별 상품 검색 실패: "+e.getMessage());
        }
    }

    @Override
    public String compose(MultipartFile roomImage, String furnitureImageUrl, String category) {
        try {
            // S3에 방 이미지 업로드
            String roomImageUrl = s3Uploader.upload("ai-compose-rooms", roomImage);

            // Replicate에 보낼 데이터 구성
            String model = "flux-kontext-apps/multi-image-kontext-pro";
            String version = "6d14f9b3d25a9400c4a5e5f0f6842ae7537fefcf68df86dad9533f66204f2bb2";

            JSONObject input = new JSONObject();
            input.put("prompt",
                    "Place the object from input_image_1 (" + category + ") naturally into the room shown in input_image_2.\n"
                            + "- Maintain the original room lighting, shadows, and colors\n"
                            + "- Do NOT modify any existing furniture or background\n"
                            + "- Position the object realistically on the room's floor plane\n"
                            + "- Match the object's perspective, angle, and orientation to the room\n"
                            + "- Apply soft shadows consistent with room lighting direction\n"
                            + "- Output should be a photorealistic composite of input_image_2 with the new object\n"
                            + "- Do NOT include side-by-side or original images, only the final composition"
            );

            input.put("input_image_1", furnitureImageUrl);
            input.put("input_image_2", roomImageUrl);
            input.put("aspect_ratio", "match_input_image");
            input.put("output_format", "png");
            input.put("seed", 42);
            input.put("safety_tolerance", 2);

            JSONObject body = new JSONObject();
            body.put("input", input);

            // Replicate 비동기 요청
            HttpClient httpClient = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.replicate.com/v1/models/" + model + "/versions/" + version + "/predictions"))
                    .header("Authorization", "Token " + replicateApiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // API 호출 실패시 처리
            if (response.statusCode() != 201) {
                throw new ZippickException(
                        ErrorCode.INTERNAL_SERVER_ERROR, "Replicate API 호출 실패: " + response.body());

            }

            // 응답에서 폴링할 URL 추출 -> status가 succeeded일때까지 폴링
            JSONObject resultJson = new JSONObject(response.body());
            String getUrl = resultJson.getJSONObject("urls").getString("get");

            // 결과 폴링 - 최대 30초까지 대기
            for (int i = 0; i < 30; i++) {
                Thread.sleep(1000); // 1초마다 polling

                HttpRequest pollRequest = HttpRequest.newBuilder()
                        .uri(new URI(getUrl))
                        .header("Authorization", "Token " + replicateApiToken)
                        .build();

                HttpResponse<String> pollResponse = httpClient.send(pollRequest, HttpResponse.BodyHandlers.ofString());
                JSONObject pollJson = new JSONObject(pollResponse.body());
                String status = pollJson.getString("status");

                // 성공 시 결과 URL 리턴
                if (status.equals("succeeded")) {
                    Object outputObj = pollJson.get("output");
                    String resultImageUrl;

                    // url이면 바로 사용
                    if (outputObj instanceof String) {
                        resultImageUrl = (String) outputObj;
                    }

                    // 배열인 경우 첫 번째 항목 사용
                    else if (outputObj instanceof JSONArray) {
                        resultImageUrl = ((JSONArray) outputObj).getString(0);
                    }
                    // 그 외는 예외 처리
                    else {
                        throw new ZippickException(ErrorCode.INTERNAL_SERVER_ERROR, "예상치 못한 output 형식: " + outputObj);
                    }

                    return resultImageUrl;
                }

                // 실패한 경우 예외 처리
                else if (status.equals("failed")) {
                    throw new ZippickException(ErrorCode.INTERNAL_SERVER_ERROR, "합성 실패: " + pollResponse.body());
                }
            }

            // 제한 시간 초과
            throw new ZippickException(ErrorCode.INTERNAL_SERVER_ERROR, "합성 시간 초과: 30초 이내에 완료되지 않음");

        } catch (IOException e) {
            throw new ZippickException(ErrorCode.FILE_UPLOAD_FAIL, "S3 파일 업로드 실패: " + e.getMessage());
        } catch (Exception e) {
            throw new ZippickException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 합성 실패: "+e.getMessage());
        }
    }

}
