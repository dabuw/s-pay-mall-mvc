package org.example.service;

import org.example.domain.req.ShopCartReq;
import org.example.domain.res.PayOrderRes;

/**
 * @author dabuw
 * @description 订单服务接口
 * @date 14/9/26
 */
public interface IOrderService {

    PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception;

}
