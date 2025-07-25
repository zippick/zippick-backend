package zippick.domain.order.Service;

import zippick.domain.order.dto.InsertOrderRequest;

public interface OrderService {
    // 주문 저장
    void insertOrder(InsertOrderRequest insertOrderRequest);
}
