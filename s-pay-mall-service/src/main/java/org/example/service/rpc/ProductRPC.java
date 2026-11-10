package org.example.service.rpc;

import org.example.domain.vo.ProductVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dabuw
 * @description 演示商品目录，后续可替换为真实商品服务
 * @date 14/9/26
 */
@Service
public class ProductRPC {

    private static final List<ProductVO> PRODUCT_CATALOG = new ArrayList<>();

    static {
        PRODUCT_CATALOG.add(build("10000124", "机械键盘 · SKU-001", "87 键热插拔轴体，PBT 键帽，Type-C 有线连接", "1.68"));
        PRODUCT_CATALOG.add(build("10000125", "无线鼠标 · SKU-002", "2.4G 无线连接，静音微动，续航 12 个月", "2.68"));
        PRODUCT_CATALOG.add(build("10000126", "桌面显示器支架 · SKU-003", "铝合金材质，单臂悬停，适配 17-32 英寸显示器", "3.68"));
    }

    private static ProductVO build(String id, String name, String desc, String price) {
        ProductVO vo = new ProductVO();
        vo.setProductId(id);
        vo.setProductName(name);
        vo.setProductDesc(desc);
        vo.setPrice(new BigDecimal(price));
        return vo;
    }

    public ProductVO queryProductByProductId(String productId) {
        for (ProductVO vo : PRODUCT_CATALOG) {
            if (vo.getProductId().equals(productId)) {
                return vo;
            }
        }
        return PRODUCT_CATALOG.get(0);
    }

    public List<ProductVO> queryProductList() {
        return new ArrayList<>(PRODUCT_CATALOG);
    }

}
