package zippick.domain.notification.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmTokenDto {
    private Long id;
    private Long memberId;
    private String fcmToken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
