package org.example.service;

import org.example.domain.req.ShopCartReq;
import org.example.domain.res.OrderDetailRes;
import org.example.domain.res.PayOrderRes;

import java.util.List;

/**
 * @author dabuw
 * @description 订单服务接口
 * @date 14/9/26
 */
public interface IOrderService {

    PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception;

    void changeOrderPaySuccess(String orderId);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);

    /** 查询单个订单 */
    OrderDetailRes queryOrder(String orderId);

    /** 查询订单列表，userId 为空时返回全部 */
    List<OrderDetailRes> queryOrderList(String userId);

}
