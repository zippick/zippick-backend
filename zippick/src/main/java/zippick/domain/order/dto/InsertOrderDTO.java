package zippick.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InsertOrderDTO {
    private String status; // FIXME: enum?
    private int totalPrice;
    private int count;
    private String merchantOrderId;
    private Long userId;
    private Long productId;
}
