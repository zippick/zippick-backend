package zippick.domain.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import zippick.domain.order.dto.InsertOrderDTO;

@Mapper
public interface OrderMapper {
    void insertOrder(InsertOrderDTO dto);
}
