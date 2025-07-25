package zippick.domain.member.service;

import zippick.domain.member.dto.request.SignUpRequest;

public interface MemberService {
    void registerMember(SignUpRequest signUpRequest);
}
