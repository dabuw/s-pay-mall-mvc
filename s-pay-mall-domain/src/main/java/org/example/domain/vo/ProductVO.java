package org.example.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author dabuw
 * @description
 * @date 14/9/26
 */
@Data
public class ProductVO {

    /** 商品ID */
    private String productId;
    /** 商品名称 */
    private String productName;
    /** 商品描述 */
    private String productDesc;
    /** 商品价格 */
    private BigDecimal price;

}