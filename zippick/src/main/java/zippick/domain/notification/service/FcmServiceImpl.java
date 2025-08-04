package zippick.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zippick.domain.notification.dto.FcmTokenDto;
import zippick.domain.notification.dto.request.FcmTokenRequest;
import zippick.domain.notification.mapper.FcmTokenMapper;

@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    private final FcmTokenMapper fcmTokenMapper;

    @Override
    @Transactional
    public void registerToken(Long memberId, FcmTokenRequest request) {

        FcmTokenDto existing = fcmTokenMapper.findByMemberId(memberId);
        if (existing == null) {
            FcmTokenDto token = FcmTokenDto.builder()
                    .memberId(memberId)
                    .fcmToken(request.getFcmToken())
                    .build();
            fcmTokenMapper.insertFcmToken(token);
        } else {
            fcmTokenMapper.updateFcmToken(memberId, request.getFcmToken());
        }
    }
}

