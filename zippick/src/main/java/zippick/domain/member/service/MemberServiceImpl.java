package zippick.domain.member.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zippick.domain.member.dto.MemberDTO;
import zippick.domain.member.dto.request.SignUpRequest;
import zippick.domain.member.dto.response.MyInfoResponse;
import zippick.domain.member.mapper.MemberMapper;
import zippick.domain.order.dto.response.OrderHistoryResponse;
import zippick.domain.order.mapper.OrderMapper;
import zippick.global.exception.ErrorCode;
import zippick.global.exception.ZippickException;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final OrderMapper orderMapper;


    @Override
    @Transactional
    public void registerMember(SignUpRequest request) {
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        memberMapper.insertMember(request);
    }

    @Override
    @Transactional
    public MyInfoResponse getMyInfo(Long memberId) {
        MemberDTO member = memberMapper.findById(memberId);
        return new MyInfoResponse(member.getName(), member.getLoginId());
    }

    @Override
    @Transactional
    public List<OrderHistoryResponse> getOrderHistories(Long memberId) {
        List<OrderHistoryResponse> ret = orderMapper.getOrderHistoriesByMemberId(memberId);
        if (ret.isEmpty()) {
            throw new ZippickException(ErrorCode.NO_ORDER_HISTORY, "주문 내역 없음");
        }
        return ret;
    }

    @Override
    @Transactional
    public boolean isEmailDuplicated(String email) {
        return memberMapper.isDuplicateLoginId(email);
    }
}
