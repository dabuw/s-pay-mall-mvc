import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000,
})

// 统一拆包：后端统一返回 { code, info, data }
http.interceptors.response.use(
  (response) => {
    const body = response.data
    // raw:true 的请求返回完整响应体，由调用方自行判断业务码
    if (response.config.raw) return body
    if (body && body.code && body.code !== '0000') {
      return Promise.reject(new Error(body.info || '请求失败'))
    }
    return body ? body.data : null
  },
  (error) => {
    const message = error?.response?.status
      ? `请求失败(${error.response.status})`
      : '网络异常，请稍后重试'
    return Promise.reject(new Error(message))
  },
)

/** 商品列表 */
export const getProductList = () => http.get('/v1/product/list')

/** 创建支付单，返回支付宝自动提交表单 HTML */
export const createPayOrder = (payload) => http.post('/v1/alipay/create_pay_order', payload)

/** 查询订单状态 */
export const getOrderStatus = (orderId) => http.get('/v1/order/status', { params: { orderId } })

/** 查询订单列表 */
export const getOrderList = (userId) => http.get('/v1/order/list', { params: { userId } })

/** 获取微信扫码登录二维码 ticket */
export const getQrCodeTicket = () => http.get('/v1/login/weixin_qrcode_ticket')

/** 轮询扫码结果，返回 { code, data } */
export const checkLogin = (ticket) =>
  http.get('/v1/login/check_login', { params: { ticket }, raw: true })
