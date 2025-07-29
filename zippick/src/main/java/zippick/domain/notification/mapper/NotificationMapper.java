package zippick.domain.notification.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zippick.domain.notification.dto.NotificationDto;

@Mapper
public interface NotificationMapper {
    void insertNotification(NotificationDto notification);

    List<NotificationDto> findNotificationsByMemberId(@Param("memberId") Long memberId);

}
