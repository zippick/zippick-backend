package zippick.domain.order.Service;

import zippick.domain.order.dto.request.InsertOrderRequest;

public interface OrderService {
    // 주문 저장
    void insertOrder(InsertOrderRequest insertOrderRequest, Long memberId);

    // 주문 취소
    void cancelOrder(Long orderId);
}
