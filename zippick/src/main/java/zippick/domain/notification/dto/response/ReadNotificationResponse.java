package zippick.domain.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import zippick.domain.notification.dto.NotificationDto;

@Getter
@AllArgsConstructor
public class ReadNotificationResponse {
    private List<NotificationDto> notifications;
}
