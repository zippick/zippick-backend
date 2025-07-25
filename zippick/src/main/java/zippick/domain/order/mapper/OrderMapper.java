package zippick.domain.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import zippick.domain.order.dto.InsertOrderDto;

@Mapper
public interface OrderMapper {
    void insertOrder(InsertOrderDto dto);
}
