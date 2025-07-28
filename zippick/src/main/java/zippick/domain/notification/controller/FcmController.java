package zippick.domain.notification.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zippick.domain.notification.dto.request.FcmTokenRequest;
import zippick.domain.notification.dto.response.FcmTokenResponse;
import zippick.domain.notification.service.FcmService;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/register")
    public ResponseEntity<FcmTokenResponse> registerFcm(HttpServletRequest request,
            @RequestBody FcmTokenRequest registerRequest) {
        Long memberId = (Long) request.getAttribute("memberId");

        if (memberId == null) {
            throw new IllegalStateException("memberId 누락: 필터에서 등록되지 않았습니다.");
        }

        fcmService.registerToken(memberId, registerRequest);

        return ResponseEntity.ok(new FcmTokenResponse(true));
    }
}

