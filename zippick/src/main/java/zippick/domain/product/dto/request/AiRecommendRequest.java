package zippick.domain.product.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
