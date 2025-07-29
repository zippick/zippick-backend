package zippick.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderDetailResponse {
    private String memberName;
    private String basicAddress;
    private String detailAddress;
    private Long productId;
    private String productImageUrl;
    private String productName;
    private Long productPrice;
    private String createdAt;
    private Long orderId;
    private String merchantOrderId;
    private Integer count;
    private Long totalPrice;
    private String status;
}
