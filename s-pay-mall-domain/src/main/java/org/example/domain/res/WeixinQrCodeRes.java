package org.example.domain.res;

import lombok.Data;

/**
 * @author dabuw
 * @description 获取微信登录二维码响应对象
 * @date 11/9/26
 */
@Data
public class WeixinQrCodeRes {

    private String ticket;
    private Long expire_seconds;
    private String url;

}
