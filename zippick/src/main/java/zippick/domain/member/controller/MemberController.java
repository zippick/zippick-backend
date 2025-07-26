package zippick.domain.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zippick.domain.member.dto.request.SignUpRequest;
import zippick.domain.member.dto.response.MyInfoResponse;
import zippick.domain.member.service.MemberService;
import zippick.domain.order.dto.response.OrderHistoryResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest request ) {
        memberService.registerMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/myinfo")
    public ResponseEntity<MyInfoResponse> getMyInfo(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        MyInfoResponse myInfo = memberService.getMyInfo(memberId);
        return ResponseEntity.ok(myInfo);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderHistoryResponse>> getOrders(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        List<OrderHistoryResponse> orderHistories = memberService.getOrderHistories(memberId);
        return ResponseEntity.ok(orderHistories);
    }
}
