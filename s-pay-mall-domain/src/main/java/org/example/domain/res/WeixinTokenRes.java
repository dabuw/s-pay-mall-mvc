package org.example.domain.res;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author dabuw
 * @description 获取 Access Token DTO 对象
 * @date 11/9/26
 */

@Data
public class WeixinTokenRes {

    private String access_token;
    private int expires_in;
    private String errcode;
    private String errmsg;

}





