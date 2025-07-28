package zippick.domain.notification.service;

import zippick.domain.notification.dto.request.FcmTokenRequest;

public interface FcmService {
    void registerToken(Long memberId, FcmTokenRequest request);
}
