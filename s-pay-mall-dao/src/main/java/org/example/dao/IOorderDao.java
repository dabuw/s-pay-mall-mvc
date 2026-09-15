package org.example.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.domain.po.PayOrder;

/**
 * @author dabuw
 * @description
 * @date 14/9/26
 */
@Mapper
public interface IOorderDao {

    void insert(PayOrder payOrder);
    PayOrder queryUnPayOrder(PayOrder payOrder);

    void updateOrderPayInfo(PayOrder payOrder);

}
