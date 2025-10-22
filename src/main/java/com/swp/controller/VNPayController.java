package com.swp.controller;

import com.swp.config.VNPayConfig;
import com.swp.entity.OrderEntity;
import com.swp.entity.PaymentEntity;
import com.swp.service.CartItemService;
import com.swp.service.OrderService;
import com.swp.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vnpay")
public class VNPayController {

    private final VNPayConfig vnPayConfig;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final CartItemService cartItemService;

    @PostMapping("/create-payment")
    @ResponseBody
    public Map<String, String> createPayment(
            @RequestParam("orderId") Long orderId,
            @RequestParam(value = "bankCode", required = false) String bankCode,
            HttpServletRequest request) throws UnsupportedEncodingException {

        Map<String, String> response = new HashMap<>();

        // Lấy order
        Optional<OrderEntity> orderOpt = orderService.findById(orderId);
        if (!orderOpt.isPresent()) {
            response.put("code", "01");
            response.put("message", "Order not found");
            return response;
        }

        OrderEntity order = orderOpt.get();

        // Tạo hoặc lấy payment
        Optional<PaymentEntity> paymentOpt = paymentService.findByOrderId(orderId);
        PaymentEntity payment;
        if (paymentOpt.isPresent()) {
            payment = paymentOpt.get();
        } else {
            payment = paymentService.createPayment(order, "VNPAY");
        }

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";

        // Số tiền cần nhân với 100 vì VNPay yêu cầu đơn vị là đồng
        long amount = order.getTotalAmount().longValue() * 100;

        String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);
        String vnp_TmnCode = vnPayConfig.getVnp_TmnCode();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", order.getOrderId().toString());
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Lưu vnpay txn ref vào payment
        paymentService.setVnpayTxnRef(payment.getPaymentId(), vnp_TxnRef);

        // Tạo payment URL
        String paymentUrl = vnPayConfig.createPaymentUrl(vnp_Params);

        response.put("code", "00");
        response.put("message", "success");
        response.put("data", paymentUrl);

        return response;
    }

    @GetMapping("/return")
    public String paymentReturn(HttpServletRequest request, Model model) {
        // Lấy tất cả parameters từ VNPay
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // Verify signature
        String signValue = vnPayConfig.hashAllFields(fields);

        String vnp_TxnRef = request.getParameter("vnp_TxnRef");
        String vnp_Amount = request.getParameter("vnp_Amount");
        String vnp_OrderInfo = request.getParameter("vnp_OrderInfo");
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
        String vnp_BankCode = request.getParameter("vnp_BankCode");
        String vnp_PayDate = request.getParameter("vnp_PayDate");
        String vnp_TransactionStatus = request.getParameter("vnp_TransactionStatus");

        boolean validSignature = signValue.equals(vnp_SecureHash);
        boolean isSuccess = "00".equals(vnp_ResponseCode);

        // Cập nhật trạng thái payment
        paymentService.updatePaymentStatus(vnp_TxnRef, vnp_ResponseCode,
                vnp_TransactionNo, vnp_BankCode, vnp_PayDate);


        // Format vnp_PayDate from yyyyMMddHHmmss to dd/MM/yyyy HH:mm
        String formattedPayDate = vnp_PayDate;
        if (vnp_PayDate != null && !vnp_PayDate.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date date = inputFormat.parse(vnp_PayDate);
                formattedPayDate = outputFormat.format(date);
            } catch (Exception e) {
                // If parsing fails, keep the original format
                formattedPayDate = vnp_PayDate;
            }
        }

        // Thêm các attributes vào model
        model.addAttribute("validSignature", validSignature);
        model.addAttribute("isSuccess", isSuccess && validSignature);
        model.addAttribute("vnp_TxnRef", vnp_TxnRef);
        model.addAttribute("vnp_Amount", Long.parseLong(vnp_Amount) / 100);
        model.addAttribute("vnp_OrderInfo", vnp_OrderInfo);
        model.addAttribute("vnp_ResponseCode", vnp_ResponseCode);
        model.addAttribute("vnp_TransactionNo", vnp_TransactionNo);
        model.addAttribute("vnp_BankCode", vnp_BankCode);
        model.addAttribute("vnp_PayDate", formattedPayDate);
        model.addAttribute("vnp_TransactionStatus", vnp_TransactionStatus);

        return "vnpay-return";
    }

    @GetMapping("/ipn")
    @ResponseBody
    public Map<String, String> ipnHandler(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();

        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        String signValue = vnPayConfig.hashAllFields(fields);

        if (signValue.equals(vnp_SecureHash)) {
            String vnp_TxnRef = request.getParameter("vnp_TxnRef");
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
            String vnp_BankCode = request.getParameter("vnp_BankCode");
            String vnp_PayDate = request.getParameter("vnp_PayDate");

            paymentService.updatePaymentStatus(vnp_TxnRef, vnp_ResponseCode,
                    vnp_TransactionNo, vnp_BankCode, vnp_PayDate);

            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
        } else {
            response.put("RspCode", "97");
            response.put("Message", "Invalid Signature");
        }

        return response;
    }
}
