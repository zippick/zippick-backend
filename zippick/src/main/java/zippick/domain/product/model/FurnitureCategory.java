package zippick.domain.product.model;

import lombok.Getter;

@Getter
public enum FurnitureCategory {

    CHAIR("의자"),
    DESK("책상"),
    SOFA("소파"),
    TABLE("식탁"),
    WARDROBE("옷장"),
    BED("침대");

    private final String korean;

    FurnitureCategory(String korean) {
        this.korean = korean;
    }

    // 영문 파라미터로부터 model 찾기
    public static FurnitureCategory fromEnglish(String english) {
        return FurnitureCategory.valueOf(english.trim().toUpperCase());
    }

    // 영문 파라미터로부터 한글 반환
    public static String toKorean(String english) {
        try {
            return fromEnglish(english).getKorean();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 카테고리입니다: " + english);
        }
    }
}
