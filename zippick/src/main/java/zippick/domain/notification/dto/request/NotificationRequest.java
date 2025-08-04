package zippick.domain.notification.dto.request;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NotificationRequest {
    private String title;
    private String content;
    private LocalDateTime createdAt;
}
