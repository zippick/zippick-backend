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
            String prompt = buildPromptForCategory(category);
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

    private String buildPromptForCategory(String category) {
        return switch (category.toLowerCase()) {
            case "의자" -> """
            Place the office chair (input_image_1) naturally into the room (input_image_2).

            - Position the chair directly in front of or slightly to the side of a desk, resting on the floor
            - Scale the chair to match the size and perspective of other nearby objects like the desk, monitor, or floor tiles
            - Ensure the seat height is aligned with the desk height (typically 70–75 cm)
            - The chair must not appear miniaturized or oversized; it should look suitable for a person to sit on
            - Align the chair’s orientation and angle with the desk and floor plane
            - Preserve existing room lighting and cast soft shadows from the same light source
            - Do NOT modify or remove existing furniture; output a realistic composite only
            - Do NOT alter any existing furniture in the room; return a single photorealistic image
            - Do NOT include the original object image as a separate element in the output
            - Do NOT generate a side-by-side, collage, or comparison view
            - The output must be a single photorealistic image with the object seamlessly integrated into the room
            """;

            case "소파" -> """
            Place the sofa (input_image_1) naturally into the room (input_image_2).

            - Position the sofa along a wall or facing a visible focal point (like a TV or window)
            - Analyze the room size and nearby furniture (e.g., coffee table, rug, wall height) to adjust the sofa’s size
            - Scale the sofa to be large enough to seat one or more people, maintaining realistic proportions
            - Match the sofa’s orientation with the wall or other seating elements
            - Keep lighting, shadows, and material consistency with the rest of the room
            - Do NOT modify or remove existing furniture; output a realistic composite only
            - Do NOT alter any existing furniture in the room; return a single photorealistic image
            - Do NOT include the original object image as a separate element in the output
            - Do NOT generate a side-by-side, collage, or comparison view
            - The output must be a single photorealistic image with the object seamlessly integrated into the room
            """;

            case "침대" -> """
            Place the bed (input_image_1) naturally into the room (input_image_2).

            - Position the bed against a wall or corner where it would typically fit based on room layout
            - Scale the bed realistically by referencing the size of other objects in the room (e.g., windows, floor tiles, shelves)
            - It must be large enough for a person to lie on (e.g., twin or queen size), not shrunken
            - Match the bed’s base to the floor perspective and align the headboard with the wall
            - Keep consistent lighting and shadows with the room’s light source
            - Do NOT remove or obscure existing objects; return a seamless photorealistic composition
            - Do NOT modify or remove existing furniture; output a realistic composite only
            - Do NOT alter any existing furniture in the room; return a single photorealistic image
            - Do NOT include the original object image as a separate element in the output
            - Do NOT generate a side-by-side, collage, or comparison view
            - The output must be a single photorealistic image with the object seamlessly integrated into the room
            """;

            case "옷장" -> """
            Place the wardrobe (input_image_1) naturally into the room (input_image_2).

            - Position the wardrobe standing against a vertical wall
            - Scale it to match the height of doors, windows, or nearby cabinets
            - The wardrobe should appear tall and upright, typically around 180–200 cm high
            - Align its vertical edges to match the room’s perspective and lines
            - Cast natural shadows onto the floor and wall consistent with the room’s lighting
            - Ensure it does not overlap or replace existing furniture
            - Do NOT modify or remove existing furniture; output a realistic composite only
            - Do NOT alter any existing furniture in the room; return a single photorealistic image
            - Do NOT include the original object image as a separate element in the output
            - Do NOT generate a side-by-side, collage, or comparison view
            - The output must be a single photorealistic image with the object seamlessly integrated into the room
            """;

            case "책상" -> """
            Place the desk (input_image_1) naturally into the room (input_image_2).

            - Position the desk against a wall or under a window, or aligned with other office elements
            - Scale the desk to match standard height (around 72–75 cm) and similar proportions to existing desks/tables
            - Align the top surface with surrounding elements like monitors, shelves, or sockets
            - Maintain the desk’s perspective, depth, and shadow realism
            - Do NOT modify or remove existing furniture; output a realistic composite only
            - Do NOT alter any existing furniture in the room; return a single photorealistic image
            - Do NOT include the original object image as a separate element in the output
            - Do NOT generate a side-by-side, collage, or comparison view
            - The output must be a single photorealistic image with the object seamlessly integrated into the room
            """;

            case "식탁" -> """
            Place the dining table (input_image_1) naturally into the room (input_image_2).

            - Position the table at the center of an open area or near the kitchen zone
            - Scale it to seat 2–6 people based on available space and existing furniture
            - Ensure its height aligns with standard dining tables (~70–75 cm) and chairs if present
            - Use visual cues like surrounding chairs, cabinets, or floor layout to determine appropriate scale
            - Maintain realistic shadowing and lighting integration
            - Do NOT modify or remove existing furniture; output a realistic composite only
            - Do NOT alter any existing furniture in the room; return a single photorealistic image
            - Do NOT include the original object image as a separate element in the output
            - Do NOT generate a side-by-side, collage, or comparison view
            - The output must be a single photorealistic image with the object seamlessly integrated into the room
            """;

            default -> """
            Place the furniture object (input_image_1) naturally into the room (input_image_2).

            - Position the object logically based on its type (e.g., floor-standing, wall-mounted)
            - Adjust the object’s size to match the spatial proportions and perspective of the room
            - Ensure the object integrates well with existing furniture and does not appear out of scale
            - Align lighting, angle, and shadows to make the result photorealistic
            - Do NOT modify or remove existing furniture; output a realistic composite only
            - Do NOT alter any existing furniture in the room; return a single photorealistic image
            - Do NOT include the original object image as a separate element in the output
            - Do NOT generate a side-by-side, collage, or comparison view
            - The output must be a single photorealistic image with the object seamlessly integrated into the room
            """;
        };
    }


}
