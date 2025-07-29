package zippick.domain.order.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zippick.domain.order.dto.InsertOrderDTO;
import zippick.domain.order.dto.request.InsertOrderRequest;
import zippick.domain.order.dto.response.OrderDetailResponse;
import zippick.domain.order.mapper.OrderMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;

    @Override
    public void insertOrder(InsertOrderRequest insertOrderRequest, Long memberId) {

        InsertOrderDTO dto = InsertOrderDTO.builder()
                        .status("ORDERED")
                        .totalPrice(insertOrderRequest.getTotalPrice())
                        .count(insertOrderRequest.getCount())
                        .merchantOrderId(insertOrderRequest.getMerchantOrderId())
                        .memberId(memberId)
                        .productId(insertOrderRequest.getProductId())
                        .build();

        orderMapper.insertOrder(dto);
    }

    @Override
    public void cancelOrder(Long orderId) {
        orderMapper.updateOrderStatusToCanceled(orderId);
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long orderId) {
        return orderMapper.getOrderDetailById(orderId);
    }
}
