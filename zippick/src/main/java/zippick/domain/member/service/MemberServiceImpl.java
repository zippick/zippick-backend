package zippick.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zippick.domain.member.dto.MemberDTO;
import zippick.domain.member.dto.request.SignUpRequest;
import zippick.domain.member.dto.response.MyInfoResponse;
import zippick.domain.member.mapper.MemberMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void registerMember(SignUpRequest request) {
        if (memberMapper.getDuplicateMember(request.getLoginId()) > 0) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        memberMapper.insertMember(request);
    }

    @Override
    public MyInfoResponse getMyInfo(Long memberId) {
        MemberDTO member = memberMapper.findById(memberId);
        return new MyInfoResponse(member.getName(), member.getLoginId());
    }
}
