package zippick.domain.product.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteriorAnalysisResponse {

    private List<PaletteColor> palette;
    private List<String> tags;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaletteColor {
        private String colorCode;
        private String colorName;
        private String toneCategory;
    }
}
