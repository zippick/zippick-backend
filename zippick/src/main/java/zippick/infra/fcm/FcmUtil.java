package zippick.infra.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Component;
import zippick.global.exception.ErrorCode;
import zippick.global.exception.ZippickException;

@Component
public class FcmUtil {

    public void sendMessageTo(String targetToken, String title, String body) {
        Message message = Message.builder()
                .setToken(targetToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new ZippickException(ErrorCode.FCM_SEND_FAIL, "FCM 전송 실패: " + e.getMessage());
        }
    }
}

