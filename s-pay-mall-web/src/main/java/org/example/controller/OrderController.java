package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.common.constants.Constants;
import org.example.common.response.Response;
import org.example.domain.res.OrderDetailRes;
import org.example.service.IOrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dabuw
 * @description 订单查询接口：支付结果轮询、我的订单
 * @date 10/11/26
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/order/")
public class OrderController {

    @Resource
    private IOrderService orderService;

    /**
     * http://localhost:8080/api/v1/order/status?orderId=xxx
     */
    @GetMapping("status")
    public Response<OrderDetailRes> queryOrderStatus(@RequestParam String orderId) {
        try {
            OrderDetailRes order = orderService.queryOrder(orderId);
            if (null == order) {
                return Response.<OrderDetailRes>builder()
                        .code(Constants.ResponseCode.UN_ERROR.getCode())
                        .info("订单不存在")
                        .build();
            }
            return Response.<OrderDetailRes>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(order)
                    .build();
        } catch (Exception e) {
            log.error("查询订单状态失败 orderId:{}", orderId, e);
            return Response.<OrderDetailRes>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * http://localhost:8080/api/v1/order/list?userId=liujiani
     */
    @GetMapping("list")
    public Response<List<OrderDetailRes>> queryOrderList(@RequestParam(required = false) String userId) {
        try {
            return Response.<List<OrderDetailRes>>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(orderService.queryOrderList(userId))
                    .build();
        } catch (Exception e) {
            log.error("查询订单列表失败 userId:{}", userId, e);
            return Response.<List<OrderDetailRes>>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}
