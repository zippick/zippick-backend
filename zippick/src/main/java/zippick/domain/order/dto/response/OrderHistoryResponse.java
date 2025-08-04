package zippick.domain.order.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
