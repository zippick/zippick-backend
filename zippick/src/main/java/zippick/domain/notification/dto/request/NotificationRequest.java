package zippick.domain.notification.dto.request;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Builder
@Getter
public class NotificationRequest {
    private String title;
    private String content;
    private LocalDateTime createdAt;
}
