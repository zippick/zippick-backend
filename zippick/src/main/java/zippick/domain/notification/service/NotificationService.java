package zippick.domain.notification.service;

import zippick.domain.notification.dto.request.NotificationRequest;
import zippick.domain.notification.dto.response.ReadNotificationResponse;
import zippick.domain.notification.dto.response.SendNotificationResponse;

public interface NotificationService {
    SendNotificationResponse sendNotification(Long memberId, NotificationRequest request);

    ReadNotificationResponse getNotifications(Long memberId, int offset);

}

