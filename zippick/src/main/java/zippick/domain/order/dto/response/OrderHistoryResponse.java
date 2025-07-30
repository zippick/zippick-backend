package zippick.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OrderHistoryResponse {
    private Long id;
    private LocalDateTime createdAt;
    private String merchantOrderId;
    private String productName;
    private String status;
    private String productImageUrl;
}
