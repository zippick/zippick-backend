package zippick.domain.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zippick.domain.order.Service.OrderService;
import zippick.domain.order.dto.request.InsertOrderRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/save")
    public ResponseEntity<Void> save(@Valid @RequestBody InsertOrderRequest request) {
        orderService.insertOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
