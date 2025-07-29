package zippick.domain.order.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zippick.domain.order.Service.OrderService;
import zippick.domain.order.dto.request.InsertOrderRequest;
import zippick.domain.order.dto.response.OrderDetailResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/save")
    public ResponseEntity<Void> save(HttpServletRequest request, @Valid @RequestBody InsertOrderRequest dto) {
        Long memberId = (Long) request.getAttribute("memberId");
        orderService.insertOrder(dto, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<Void> cancel(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/detail/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        OrderDetailResponse response = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(response);
    }
}
