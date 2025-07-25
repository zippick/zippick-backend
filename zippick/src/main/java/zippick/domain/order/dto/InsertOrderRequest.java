package zippick.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InsertOrderRequest {
    /*
    * 총합가격, 수량, 가맹점 결제 식별번호, 회원번호, 상품번호
    * */
    private int totalPrice;
    private int count;
    private String merchantOrderId;
    private Long userId;
    private Long productId;
}
