package zippick.domain.notification.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import zippick.domain.notification.dto.NotificationDto;

@Getter
@AllArgsConstructor
public class ReadNotificationResponse {
    private List<NotificationDto> notifications;
}
