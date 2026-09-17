package org.example.job;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.service.IOrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dabuw
 * @description 检测未接收到或未正确处理的支付回调通知
 * @date 16/9/26
 */
@Slf4j
@Component()
public class NoPayNotifyOrderJob {

    @Resource
    private IOrderService orderService;
    @Resource
    private AlipayClient alipayClient;

    @Scheduled(cron = "0/3 * * * * ?")
    public void exec() {
        try {
            log.info("任务；检测未接收到或未正确处理的支付回调通知");
            List<String> orderIds = orderService.queryNoPayNotifyOrder();
            if (null == orderIds || orderIds.isEmpty()) {
                log.info("任务；暂无待查询的未支付订单");
                return;
            }

            for (String orderId : orderIds) {
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                AlipayTradeQueryModel bizModel = new AlipayTradeQueryModel();
                bizModel.setOutTradeNo(orderId);
                request.setBizModel(bizModel);

                AlipayTradeQueryResponse alipayTradeQueryResponse = alipayClient.execute(request);
                String code = alipayTradeQueryResponse.getCode();
                String tradeStatus = alipayTradeQueryResponse.getTradeStatus();
                log.info("支付宝订单查询 orderId:{} code:{} subCode:{} msg:{} tradeStatus:{}",
                        orderId,
                        code,
                        alipayTradeQueryResponse.getSubCode(),
                        alipayTradeQueryResponse.getMsg(),
                        tradeStatus);

                if ("10000".equals(code)
                        && ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))) {
                    orderService.changeOrderPaySuccess(orderId);
                    log.info("支付宝订单查询成功，更新本地订单为已支付 orderId:{}", orderId);
                }
            }
        } catch (Exception e) {
            log.error("检测未接收到或未正确处理的支付回调通知失败", e);
        }
    }

}
