package zippick.domain.member.service;

import zippick.member.dto.request.SignUpRequest;

public interface MemberService {
    void registerMember(SignUpRequest signUpRequest);
}
