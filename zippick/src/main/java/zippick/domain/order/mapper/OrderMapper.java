package zippick.domain.order.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import zippick.domain.order.dto.InsertOrderDTO;
import zippick.domain.order.dto.response.OrderDetailResponse;
import zippick.domain.order.dto.response.OrderHistoryResponse;

@Mapper
public interface OrderMapper {
    void insertOrder(InsertOrderDTO dto);
    List<OrderHistoryResponse> getOrderHistoriesByMemberId(Long memberId);
    void updateOrderStatusToCanceled(Long orderId);
    OrderDetailResponse getOrderDetailById(Long orderId);
}
