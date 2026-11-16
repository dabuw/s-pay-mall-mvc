# 模拟支付商城 · MVC 版

同一套支付业务的 **MVC 分层**实现，用于和 DDD 版本做对照。

> DDD 版本：[s-pay-mall-ddd](https://github.com/dabuw/s-pay-mall-ddd)
> 在线演示：[mall.dabuw.online](https://mall.dabuw.online)（部署的是 DDD 版）

## 这个仓库的定位

支付链路的业务逻辑（下单、支付、回调、补偿）和 DDD 版完全一致，区别只在于**代码组织方式**：

| 维度 | MVC 版（本仓库） | DDD 版 |
| --- | --- | --- |
| 模块划分 | `common` / `domain` / `dao` / `service` / `web` | `api` / `types` / `domain` / `infrastructure` / `trigger` / `app` |
| 分层依据 | 技术分层（Controller → Service → DAO） | 业务边界 + 依赖倒置 |
| 业务逻辑位置 | 集中在 `OrderServiceImpl` | 聚合、实体、领域服务 |
| 依赖方向 | web → service → dao → domain | domain 不依赖外层，infrastructure 实现 domain 接口 |
| 单元测试 | 需要 Spring 上下文 | domain 可纯 JVM 测试 |
| 适合场景 | 业务简单、交付快、CRUD 多 | 规则复杂、长期演进、多人协作 |

## 结构

```
s-pay-mall-web        Controller、定时任务、启动类、配置
s-pay-mall-service    Service 接口与实现（业务逻辑）
s-pay-mall-dao        MyBatis Mapper 接口
s-pay-mall-domain     PO / VO / DTO
s-pay-mall-common     常量、异常、响应体、微信工具
```

## 技术栈

Java 8 · Spring Boot 2.7.12 · MyBatis 2.1.4 · MySQL 8 · alipay-sdk-java 4.38.157 · Guava · Retrofit 2.9
前端：Vue3 + Vite（`s-pay-mall-ui/`）

## 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/product/list` | 商品列表 |
| POST | `/api/v1/alipay/create_pay_order` | 下单并返回支付宝表单 |
| POST | `/api/v1/alipay/alipay_notify_url` | 支付宝异步通知 |
| GET | `/api/v1/order/status` | 订单状态 |
| GET | `/api/v1/order/list` | 订单列表 |
| GET | `/api/v1/login/weixin_qrcode_ticket` | 微信扫码登录二维码 |
| GET | `/api/v1/login/check_login` | 轮询扫码结果 |
| GET / POST | `/api/v1/weixin/portal/receive` | 微信服务器校验 / 事件回调 |

## 本地启动

```bash
# 1. 数据库（与 DDD 版共用同一张表结构）
mysql -uroot -p < ../s-pay-mall-ddd-ljn/docs/dev-ops/mysql/sql/s-pay-mall.sql

# 2. 后端（默认 8080）
mvn -DskipTests package
java -jar s-pay-mall-web/target/s-pay-mall-mvc-app.jar

# 3. 前端
cd s-pay-mall-ui
npm install
npm run dev
```

## 说明

- 支付宝沙箱环境，只能使用沙箱买家账号，没有真实资金
- 私钥、公钥、公众号 secret 通过环境变量注入，不提交到仓库
- 本项目仅用于学习与演示
