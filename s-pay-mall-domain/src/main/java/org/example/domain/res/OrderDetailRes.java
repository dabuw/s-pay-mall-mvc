package org.example.domain.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author dabuw
 * @description 订单详情（供前端展示用）
 * @date 10/11/26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailRes {

    private String productId;
    private String productName;
    private String orderId;
    private Date orderTime;
    private BigDecimal totalAmount;
    /** 状态码：CREATE / PAY_WAIT / PAY_SUCCESS / DEAL_DONE / CLOSE */
    private String orderStatusVO;
    private String payUrl;

}
