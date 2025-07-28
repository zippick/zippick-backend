package zippick.domain.notification.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zippick.domain.notification.dto.request.NotificationRequest;
import zippick.domain.notification.dto.response.ReadNotificationResponse;
import zippick.domain.notification.dto.response.SendNotificationResponse;
import zippick.domain.notification.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<SendNotificationResponse> sendNotification(
            HttpServletRequest request,
            @RequestBody NotificationRequest reqBody) {

        Long memberId = (Long) request.getAttribute("memberId");
        if (memberId == null) {
            throw new IllegalStateException("memberId가 누락되었습니다.");
        }

        SendNotificationResponse response = notificationService.sendNotification(memberId, reqBody);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ReadNotificationResponse> getNotifications(
            HttpServletRequest request,
            @RequestParam int offset) {

        Long memberId = (Long) request.getAttribute("memberId");
        if (memberId == null) {
            throw new IllegalStateException("memberId가 누락되었습니다.");
        }

        ReadNotificationResponse response = notificationService.getNotifications(memberId, offset);
        return ResponseEntity.ok(response);
    }
}
