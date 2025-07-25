package zippick.domain.member.service;

import zippick.domain.member.dto.request.SignUpRequest;
import zippick.domain.member.dto.response.MyInfoResponse;

public interface MemberService {
    void registerMember(SignUpRequest signUpRequest);
    MyInfoResponse getMyInfo(Long memberId);

}
