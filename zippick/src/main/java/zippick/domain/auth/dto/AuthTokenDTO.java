package zippick.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AuthTokenDTO {
    private String token;
    private LocalDateTime expiredAt;
    private Long memberId;
}
