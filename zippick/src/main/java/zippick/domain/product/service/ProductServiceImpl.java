package zippick.domain.product.service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zippick.domain.product.dto.ProductLikedDto;
import zippick.domain.product.dto.request.AiRecommendRequest;
import zippick.domain.product.dto.response.InteriorAnalysisResponse;
import zippick.domain.product.dto.response.ProductDetailResponse;
import zippick.domain.product.dto.ProductDto;
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

    @Value("${openai.api-key}")
    private String openaiApiKey;

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductsByKeyword(String keyword, String category, String sort, Long offset) {
        try {
            long limit = 4;

            List<ProductDto> products = productMapper.findProductsByKeywordAndCategoryAndSort(keyword, category, sort, offset, limit);
            long totalCount = productMapper.countProductsByKeywordAndCategory(keyword, category);

            return ProductResponse.builder()
                    .products(products)
                    .totalCount(totalCount)
                    .build();
        } catch (Exception e) {
            throw new ZippickException(ErrorCode.INTERNAL_SERVER_ERROR, "키워드+카테고리로 상품 검색 실패: " + e.getMessage());
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
            String prompt = String.format(
                    "Place the object from input_image_1 (\"%s\") naturally into the room shown in input_image_2.\n"
                            + "- The object must be realistically positioned on the floor or against a wall, depending on its type\n"
                            + "- Analyze existing furniture and architecture in the room to determine a suitable location and realistic scale\n"
                            + "- Adjust the size of the object so it appears proportionate and consistent with surrounding furniture\n"
                            + "- Align the perspective, angle, and orientation of the object to match the room’s camera view\n"
                            + "- Maintain the original lighting and shadows of the room; cast soft and realistic shadows for the new object\n"
                            + "- Do NOT remove or alter any existing furniture or decor\n"
                            + "- Do NOT generate comparison or side-by-side images; only output a photorealistic single-frame result",
                    category
            );
            input.put("prompt", prompt);

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

    @Override
    public ProductDetailResponse getProductDetailById(Long id) {
        ProductDetailResponse response = productMapper.findProductDetailById(id);
        if (response == null) {
            throw new ZippickException(ErrorCode.INTERNAL_SERVER_ERROR, "해당 상품이 존재하지 않음");
        }
        return response;
    }

    @Override
    public List<ProductLikedDto> getProductsByIds(List<Long> ids) {
        // 리스트가 빈 상태로 요청되는 경우
        if (ids == null || ids.isEmpty()) {
            throw new ZippickException(ErrorCode.ILLEGAL_ARGUMENT, "상품 ID 리스트가 비어 있습니다.");
        }

        List<ProductLikedDto> products = productMapper.findProductsByIds(ids);

        // 데이터를 찾을 수 없는 경우
         if (products.isEmpty()) {
             throw new ZippickException(ErrorCode.LIKED_NOT_FOUND);
         }

        return products;
    }

    @Override
    public InteriorAnalysisResponse analysisInteriorImage(MultipartFile roomImage) {
        try {
            // 1. S3에 이미지 업로드
            String imageUrl = s3Uploader.upload("ai-interior-rooms", roomImage);

            // 2. 프롬프트 구성
            String prompt = """
            Please analyze the uploaded room image and respond **strictly** in the following JSON format without any explanation:
            
            1. Three matching interior color palettes.
               - Each palette must include:
                 - "code" (hex color)
                 - "name" in Korean
                 - "toneCategory" selected from the list below:
                   ["화이트/베이지", "그레이", "블루/네이비", "브라운/우드", "블랙"]
            
            2. Two most suitable style tags from the list below:
               ["내추럴", "모던&시크", "빈티지&레트로", "클래식", "심플&미니멀"]
            
            Respond strictly in the following JSON format:
            
            {
              "palette": [
                { "code": "#E4D9C2", "name": "우드 베이지", "toneCategory": "브라운/우드" },
                { "code": "#B1956C", "name": "딥 샌드", "toneCategory": "브라운/우드" },
                { "code": "#F5F5F5", "name": "뉴트럴 화이트", "toneCategory": "화이트/베이지" }
              ],
              "tags": ["내추럴", "심플&미니멀"]
            }
            
            Do not include any explanation or additional text.
            """;

            // 3. HTTP 요청 준비 (Java HttpClient 사용)
            HttpClient httpClient = HttpClient.newHttpClient();

            JSONObject imageNode = new JSONObject();
            imageNode.put("type", "image_url");
            JSONObject imageUrlNode = new JSONObject();
            imageUrlNode.put("url", imageUrl);
            imageNode.put("image_url", imageUrlNode);

            JSONObject textNode = new JSONObject();
            textNode.put("type", "text");
            textNode.put("text", prompt);

            JSONArray contentArray = new JSONArray();
            contentArray.put(textNode);
            contentArray.put(imageNode);

            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", contentArray);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4o");
            requestBody.put("max_tokens", 1000);
            requestBody.put("messages", new JSONArray().put(message));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            // 4. 응답 처리
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ZippickException(ErrorCode.INTERNAL_SERVER_ERROR, "OpenAI Vision 호출 실패: " + response.body());
            }

            JSONObject body = new JSONObject(response.body());
            String content = body.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            System.out.println("GPT 응답 원문:\n" + content);

            // 마크다운 제거
            String cleanedContent = content
                    .replaceAll("(?i)```json", "")
                    .replaceAll("```", "")
                    .trim();

            return parseGptFormattedResponse(cleanedContent);

        } catch (IOException e) {
            throw new ZippickException(ErrorCode.FILE_UPLOAD_FAIL, "S3 업로드 실패: " + e.getMessage());
        } catch (Exception e) {
            throw new ZippickException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 분석 실패: " + e.getMessage());
        }
    }

    private InteriorAnalysisResponse parseGptFormattedResponse(String content) throws JSONException {
        // content는 이미 JSON 문자열이므로 그대로 파싱
        JSONObject json = new JSONObject(content);

        List<InteriorAnalysisResponse.PaletteColor> palette = new ArrayList<>();
        JSONArray paletteArray = json.getJSONArray("palette");

        for (int i = 0; i < paletteArray.length(); i++) {
            JSONObject colorObj = paletteArray.getJSONObject(i);
            palette.add(InteriorAnalysisResponse.PaletteColor.builder()
                    .colorCode(colorObj.getString("code"))
                    .colorName(colorObj.getString("name"))
                    .toneCategory(colorObj.getString("toneCategory"))
                    .build());
        }

        List<String> tags = new ArrayList<>();
        JSONArray tagsArray = json.getJSONArray("tags");
        for (int i = 0; i < tagsArray.length(); i++) {
            tags.add(tagsArray.getString(i));
        }

        return InteriorAnalysisResponse.builder()
                .palette(palette)
                .tags(tags)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductsByCategoryAndPrice(String category, Long minPrice, Long maxPrice, String sort, Long offset) {
        try {
            long limit = 4;

            List<ProductDto> products = productMapper.findProductsByCategoryAndPrice(
                    category, minPrice, maxPrice, sort, offset, limit
            );

            long totalCount = productMapper.countProductsByCategoryAndPrice(
                    category, minPrice, maxPrice
            );

            return ProductResponse.builder()
                    .products(products)
                    .totalCount(totalCount)
                    .build();
        } catch (Exception e) {
            throw new ZippickException(ErrorCode.INTERNAL_SERVER_ERROR, "카테고리+가격 조건 상품 검색 실패: " + e.getMessage());
        }
    }

    @Override
    public List<ProductLikedDto> recommend(AiRecommendRequest request) {
        return switch (request.getRecommendType()) {
            case "COLOR" -> productMapper.findByCategoryAndTone(request.getCategory(), request.getToneCategories());
            case "STYLE" -> productMapper.findByCategoryAndTags(request.getCategory(), request.getTags());
            default -> throw new ZippickException(ErrorCode.ILLEGAL_ARGUMENT);
        };
    }

}
