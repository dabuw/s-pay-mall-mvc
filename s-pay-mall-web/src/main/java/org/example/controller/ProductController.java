package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.common.constants.Constants;
import org.example.common.response.Response;
import org.example.domain.vo.ProductVO;
import org.example.service.rpc.ProductRPC;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dabuw
 * @description 商品查询接口（演示目录）
 * @date 10/11/26
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/product/")
public class ProductController {

    @Resource
    private ProductRPC productRPC;

    @GetMapping("list")
    public Response<List<ProductVO>> queryProductList() {
        try {
            return Response.<List<ProductVO>>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(productRPC.queryProductList())
                    .build();
        } catch (Exception e) {
            log.error("查询商品列表失败", e);
            return Response.<List<ProductVO>>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}
