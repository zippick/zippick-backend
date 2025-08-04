package zippick.domain.member.service;

import java.util.List;
import zippick.domain.member.dto.request.SignUpRequest;
import zippick.domain.member.dto.response.MyInfoResponse;
import zippick.domain.order.dto.response.OrderHistoryResponse;

public interface MemberService {
    void registerMember(SignUpRequest signUpRequest);
    MyInfoResponse getMyInfo(Long memberId);
    List<OrderHistoryResponse> getOrderHistories(Long memberId);
    boolean isEmailDuplicated(String email);
}
