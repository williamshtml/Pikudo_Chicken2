package com.pikudo.restaurant.service.orders;

import com.pikudo.restaurant.dto.orders.OrderDiscountRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderDiscountResponseDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentResponseDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentSummaryDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentVoidRequestDTO;
import com.pikudo.restaurant.entity.Pedido;

import java.util.List;

public interface OrderPaymentService {

    OrderPaymentResponseDTO addPayment(Long orderId, OrderPaymentRequestDTO request);

    List<OrderPaymentResponseDTO> listPayments(Long orderId);

    OrderPaymentSummaryDTO getSummary(Long orderId);

    OrderPaymentSummaryDTO summarize(Pedido pedido);

    OrderPaymentResponseDTO voidPayment(Long orderId, Long paymentId, OrderPaymentVoidRequestDTO request);

    OrderDiscountResponseDTO applyDiscount(Long orderId, OrderDiscountRequestDTO request);

    List<OrderDiscountResponseDTO> listDiscounts(Long orderId);

    void refreshPaymentStatus(Pedido pedido);
}
