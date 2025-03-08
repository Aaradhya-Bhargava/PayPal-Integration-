package com.cpt.payments.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cpt.payments.constant.Constant;
import com.cpt.payments.constant.ErrorCodeEnum;
import com.cpt.payments.dto.CreateOrderReqDTO;
import com.cpt.payments.dto.OrderDTO;
import com.cpt.payments.exception.PaypalProviderException;
import com.cpt.payments.http.HttpClientUtil;
import com.cpt.payments.http.HttpRequest;
import com.cpt.payments.paypal.PaypalErrorResponse;
import com.cpt.payments.paypal.res.Link;
import com.cpt.payments.paypal.res.OrderResponse;
import com.cpt.payments.service.helper.CaptureOrderRequestHelper;
import com.cpt.payments.service.helper.CreateOrderRequestHelper;
import com.cpt.payments.service.helper.GetOrderRequestHelper;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private HttpClientUtil httpClientUtil;

    @Mock
    private CreateOrderRequestHelper createOrderRequestHelper;

    @Mock
    private GetOrderRequestHelper getOrderRequestHelper;

    @Mock
    private CaptureOrderRequestHelper captureOrderRequestHelper;

    @Mock
    private Gson gson;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private CreateOrderReqDTO createOrderReqDTO;
    private HttpRequest httpRequest;

    @BeforeEach
    public void setUp() {
        createOrderReqDTO = new CreateOrderReqDTO();
        httpRequest = new HttpRequest();
    }

    // Test cases for createOrder method
    @Test
    public void testCreateOrder_Success() {
        OrderResponse mockOrderResponse = createMockOrderResponse("123", "CREATED");
        ResponseEntity<String> responseEntity = new ResponseEntity<>(new Gson().toJson(mockOrderResponse), HttpStatus.OK);

        when(createOrderRequestHelper.prepareRequest(any())).thenReturn(httpRequest);
        when(httpClientUtil.makeHttpRequest(any())).thenReturn(responseEntity);
        when(gson.fromJson(anyString(), eq(OrderResponse.class))).thenReturn(mockOrderResponse);

        OrderDTO result = paymentService.createOrder(createOrderReqDTO);

        assertNotNull(result);
        assertEquals("123", result.getId());
        assertEquals("CREATED", result.getStatus());
        assertEquals("https://redirect-url.com", result.getRedirectUrl());
    }

    @Test
    public void testCreateOrder_NullResponse() {
        // Arrange
        when(createOrderRequestHelper.prepareRequest(any())).thenReturn(httpRequest);
        when(httpClientUtil.makeHttpRequest(any())).thenReturn(null);

        // Act & Assert
        PaypalProviderException exception = assertThrows(
            PaypalProviderException.class,
            () -> paymentService.createOrder(createOrderReqDTO)
        );

        // Verify that the exception has the correct error code and message
        assertEquals(ErrorCodeEnum.INVALID_RESPONSE_FROM_PAYPAL.getErrorCode(), exception.getErrorCode());
        assertEquals(ErrorCodeEnum.INVALID_RESPONSE_FROM_PAYPAL.getErrorMessage(), exception.getErrorMessage());
    }


    @Test
    public void testCreateOrder_InvalidJsonResponse() {
        String invalidJson = "{invalid";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidJson, HttpStatus.OK);

        when(createOrderRequestHelper.prepareRequest(any())).thenReturn(httpRequest);
        when(httpClientUtil.makeHttpRequest(any())).thenReturn(responseEntity);
        when(gson.fromJson(anyString(), eq(OrderResponse.class))).thenThrow(new RuntimeException("Json parsing error"));

        assertThrows(RuntimeException.class, () -> paymentService.createOrder(createOrderReqDTO));
    }

    // Test cases for captureOrder method
    @Test
    public void testCaptureOrder_Success() {
        OrderResponse mockOrderResponse = createMockOrderResponse("456", "CAPTURED");
        ResponseEntity<String> responseEntity = new ResponseEntity<>(new Gson().toJson(mockOrderResponse), HttpStatus.OK);

        when(captureOrderRequestHelper.prepareRequest(anyString())).thenReturn(httpRequest);
        when(httpClientUtil.makeHttpRequest(any())).thenReturn(responseEntity);
        when(gson.fromJson(anyString(), eq(OrderResponse.class))).thenReturn(mockOrderResponse);

        OrderDTO result = paymentService.captureOrder("456");

        assertNotNull(result);
        assertEquals("456", result.getId());
        assertEquals("CAPTURED", result.getStatus());
        assertEquals("https://redirect-url.com", result.getRedirectUrl());
    }

    @Test
    public void testCaptureOrder_NullResponse() {
        when(captureOrderRequestHelper.prepareRequest(anyString())).thenReturn(httpRequest);
        when(httpClientUtil.makeHttpRequest(any())).thenReturn(null);

        assertThrows(PaypalProviderException.class, () -> paymentService.captureOrder("123"));
    }

    @Test
    public void testCaptureOrder_InvalidJsonResponse() {
        String invalidJson = "{invalid";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidJson, HttpStatus.OK);

        when(captureOrderRequestHelper.prepareRequest(anyString())).thenReturn(httpRequest);
        when(httpClientUtil.makeHttpRequest(any())).thenReturn(responseEntity);
        when(gson.fromJson(anyString(), eq(OrderResponse.class))).thenThrow(new RuntimeException("Json parsing error"));

        assertThrows(RuntimeException.class, () -> paymentService.captureOrder("123"));
    }

    // Test cases for getOrder method
    @Test
    public void testGetOrder_Success() {
        OrderResponse mockOrderResponse = createMockOrderResponse("789", "APPROVED");
        ResponseEntity<String> responseEntity = new ResponseEntity<>(new Gson().toJson(mockOrderResponse), HttpStatus.OK);

        when(getOrderRequestHelper.prepareRequest(anyString())).thenReturn(httpRequest);
        when(httpClientUtil.makeHttpRequest(any())).thenReturn(responseEntity);
        when(gson.fromJson(anyString(), eq(OrderResponse.class))).thenReturn(mockOrderResponse);

        OrderDTO result = paymentService.getOrder("789");

        assertNotNull(result);
        assertEquals("789", result.getId());
        assertEquals("APPROVED", result.getStatus());
        assertEquals("https://redirect-url.com", result.getRedirectUrl());
    }

    @Test
    public void testGetOrder_NullResponse() {
        when(getOrderRequestHelper.prepareRequest(anyString())).thenReturn(httpRequest);
        when(httpClientUtil.makeHttpRequest(any())).thenReturn(null);

        assertThrows(PaypalProviderException.class, () -> paymentService.getOrder("789"));
    }

    @Test
    public void testGetOrder_HttpErrorResponse() {
        PaypalErrorResponse mockErrorResponse = new PaypalErrorResponse();
        mockErrorResponse.setError("ORDER_NOT_FOUND");
        mockErrorResponse.setError_description("Order not found");
        String errorResponseJson = new Gson().toJson(mockErrorResponse);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(errorResponseJson, HttpStatus.NOT_FOUND);

        when(getOrderRequestHelper.prepareRequest(anyString())).thenReturn(httpRequest);
        when(httpClientUtil.makeHttpRequest(any())).thenReturn(responseEntity);
        when(gson.fromJson(anyString(), eq(PaypalErrorResponse.class))).thenReturn(mockErrorResponse);

        PaypalProviderException exception = assertThrows(
            PaypalProviderException.class,
            () -> paymentService.getOrder("789")
        );

        assertEquals("ORDER_NOT_FOUND", exception.getErrorCode());
        assertEquals("Order not found", exception.getErrorMessage());
    }
    
    private OrderResponse createMockOrderResponse(String id, String status) {
        OrderResponse response = new OrderResponse();
        response.setId(id);
        response.setStatus(status);

        Link link = new Link();
        link.setRel(Constant.REDIRECT_URL_PAYER_ACTION);
        link.setHref("https://redirect-url.com");
        response.setLinks(List.of(link));

        return response;
    }
}
