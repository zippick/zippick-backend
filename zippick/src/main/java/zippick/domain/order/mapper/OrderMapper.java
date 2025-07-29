package zippick.domain.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import zippick.domain.order.dto.InsertOrderDTO;
import zippick.domain.order.dto.response.OrderDetailResponse;
import zippick.domain.order.dto.response.OrderHistoryResponse;

import java.util.List;

@Mapper
public interface OrderMapper {
    void insertOrder(InsertOrderDTO dto);
    List<OrderHistoryResponse> getOrderHistoriesByMemberId(Long memberId);
    void updateOrderStatusToCanceled(Long orderId);
    OrderDetailResponse getOrderDetailById(Long orderId);
}
