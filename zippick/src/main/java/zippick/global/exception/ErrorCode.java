package zippick.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    EXPIRED_TOKEN("4000", "토큰 만료", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("4002", "유효하지 않은 토큰", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("4003", "접근 권한이 없음", HttpStatus.FORBIDDEN),
    ENTITY_NOT_FOUND("4004", "엔티티 없음", HttpStatus.BAD_REQUEST),
    ILLEGAL_ARGUMENT("4005", "적절하지 않은 인자", HttpStatus.BAD_REQUEST),
    MISSING_TOKEN("4006", "토큰 없음", HttpStatus.UNAUTHORIZED),
    INVALID_REQUEST_PARAMS("4007", "잘못된 요청 파라미터", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("5000", "서버 에러", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_SEND_FAIL("5001", "이메일 전송 실패", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_FAIL("5002", "파일 업로드 실패", HttpStatus.INTERNAL_SERVER_ERROR),
    NO_ORDER_HISTORY("5003", "주문 내역 없음", HttpStatus.NOT_FOUND),
    LIKED_NOT_FOUND("5004", "찜 조회 실패", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
