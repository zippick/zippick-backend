package zippick.domain.notification.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zippick.domain.notification.dto.FcmTokenDto;

@Mapper
public interface FcmTokenMapper {
    void insertFcmToken(@Param("fcmToken") FcmTokenDto token);

    void updateFcmToken(@Param("memberId") Long memberId, @Param("fcmToken") String fcmToken);

    FcmTokenDto findByMemberId(@Param("memberId") Long memberId);
}
