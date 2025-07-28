package zippick.domain.product.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendRequest {
    private String category;
    private String recommendType; // COLOR or STYLE
    private List<String> toneCategories;
    private List<String> tags;
}
