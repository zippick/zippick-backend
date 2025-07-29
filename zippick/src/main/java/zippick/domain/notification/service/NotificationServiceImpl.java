package zippick.domain.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zippick.domain.notification.dto.NotificationDto;
import zippick.domain.notification.dto.request.NotificationRequest;
import zippick.domain.notification.dto.response.ReadNotificationResponse;
import zippick.domain.notification.dto.response.SendNotificationResponse;
import zippick.domain.notification.mapper.FcmTokenMapper;
import zippick.domain.notification.mapper.NotificationMapper;
import zippick.infra.fcm.FcmUtil;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final FcmTokenMapper fcmTokenMapper;
    private final FcmUtil fcmUtil;

    @Override
    @Transactional
    public SendNotificationResponse sendNotification(Long memberId, NotificationRequest request) {

        NotificationDto notification = NotificationDto.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .createdAt(request.getCreatedAt())
                .createdBy(memberId)
                .memberId(memberId)
                .build();

        notificationMapper.insertNotification(notification);

        String fcmToken = fcmTokenMapper.findByMemberId(memberId).getFcmToken();
        fcmUtil.sendMessageTo(fcmToken, request.getTitle(), request.getContent());

        return new SendNotificationResponse(true, notification.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ReadNotificationResponse getNotifications(Long memberId) {
        List<NotificationDto> list = notificationMapper.findNotificationsByMemberId(memberId);
        return new ReadNotificationResponse(list);
    }


}
