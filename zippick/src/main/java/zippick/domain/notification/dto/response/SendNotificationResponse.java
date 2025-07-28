package zippick.domain.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SendNotificationResponse {
    private boolean success;
    private Long notificationId;
}
