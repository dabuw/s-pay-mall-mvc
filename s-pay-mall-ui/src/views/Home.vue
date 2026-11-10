<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { createPayOrder, getProductList } from '../api'
import { formatAmount } from '../utils/order'
import { user } from '../stores/user'

const router = useRouter()
const products = ref([])
const loading = ref(true)
const error = ref('')
const buyingId = ref('')

const productEmoji = {
  '10000124': '⌨️',
  '10000125': '🖱️',
  '10000126': '🖥️',
}

async function loadProducts() {
  loading.value = true
  error.value = ''
  try {
    products.value = await getProductList()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function extractOrderId(html) {
  const decoded = String(html).replace(/&quot;/g, '"')
  const matched = decoded.match(/"out_trade_no"\s*:\s*"([^"]+)"/)
  return matched ? matched[1] : ''
}

async function buy(product) {
  if (buyingId.value) return
  if (!user.userId) {
    router.push({ name: 'login', query: { redirect: '/' } })
    return
  }

  buyingId.value = product.productId
  error.value = ''
  try {
    const formHtml = await createPayOrder({
      userId: user.userId,
      productId: product.productId,
    })
    const orderId = extractOrderId(formHtml)

    // 新窗口渲染支付宝自动提交表单，当前页留在支付结果页轮询
    const payWindow = window.open('', '_blank')
    if (payWindow) {
      payWindow.document.open()
      payWindow.document.write(formHtml)
      payWindow.document.close()
    } else {
      // 弹窗被拦截：不跳走，留在商城，由结果页的「打开支付页」按钮兜底
      console.warn('支付页弹窗被拦截')
    }

    if (orderId) {
      router.push({
        name: 'result',
        params: { orderId },
        query: payWindow ? {} : { blocked: '1' },
      })
    }
  } catch (e) {
    error.value = e.message
  } finally {
    buyingId.value = ''
  }
}

onMounted(loadProducts)
</script>

<template>
  <div class="page">
    <div class="container">
      <section class="hero card">
        <div>
          <p class="hero-badge">支付宝沙箱 · 电脑网站支付</p>
          <h1 class="hero-title">选一件商品，体验完整的支付链路</h1>
          <p class="hero-desc">
            下单 → 创建支付单 → 支付宝收银台 → 异步回调 → 订单状态更新，全流程由 MVC 分层架构驱动，登录使用微信扫码。
          </p>
        </div>
        <div class="hero-user">
          <p class="hero-label">当前用户</p>
          <div v-if="user.userId" class="hero-user-info">
            <span class="hero-avatar">{{ user.guest ? '👤' : '💬' }}</span>
            <div>
              <strong>{{ user.nickname || '微信用户' }}</strong>
              <p class="hero-uid">{{ user.userId }}</p>
            </div>
          </div>
          <RouterLink v-else :to="{ name: 'login', query: { redirect: '/' } }" class="btn" style="width: 100%">
            微信扫码登录
          </RouterLink>
          <p class="hero-tip">登录后订单会绑定到你的微信账号</p>
        </div>
      </section>

      <section class="pay-tip card">
        <div class="pay-tip-head">
          <span class="pay-tip-icon">💡</span>
          <strong>支付宝沙箱测试账号</strong>
          <span class="pay-tip-sub">点击「立即购买」跳转收银台后，用下面这组账号登录并付款</span>
        </div>
        <div class="pay-tip-grid">
          <div><span>登录账号</span><b>orbyqu7212@sandbox.com</b></div>
          <div><span>登录密码</span><b>111111</b></div>
          <div><span>支付密码</span><b>111111</b></div>
        </div>
      </section>

      <div class="list-head">
        <h2>商品列表</h2>
        <span class="list-count">共 {{ products.length }} 件</span>
      </div>

      <p v-if="error" class="alert">{{ error }}</p>

      <div v-if="loading" class="grid">
        <div v-for="n in 3" :key="n" class="card product">
          <div class="skeleton product-thumb" />
          <div class="product-body">
            <div class="skeleton" style="height: 18px; width: 70%" />
            <div class="skeleton" style="height: 14px; margin-top: 10px" />
            <div class="skeleton" style="height: 14px; margin-top: 8px; width: 80%" />
          </div>
        </div>
      </div>

      <div v-else-if="products.length" class="grid">
        <article v-for="product in products" :key="product.productId" class="card product">
          <div class="product-thumb">
            <span>{{ productEmoji[product.productId] || '📦' }}</span>
          </div>
          <div class="product-body">
            <h3 class="product-name">{{ product.productName }}</h3>
            <p class="product-desc">{{ product.productDesc }}</p>
            <div class="product-foot">
              <span class="price">
                <em>¥</em>{{ formatAmount(product.price) }}
              </span>
              <button class="btn btn-sm" :disabled="!!buyingId" @click="buy(product)">
                {{ buyingId === product.productId ? '创建支付单…' : '立即购买' }}
              </button>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="card empty">
        <div class="empty-icon">📦</div>
        <p>暂无商品</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hero {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 32px;
  padding: 32px;
  margin-bottom: 32px;
  background: linear-gradient(135deg, #ffffff 0%, #f7fbff 100%);
}

.hero-badge {
  display: inline-block;
  margin: 0 0 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(22, 119, 255, 0.1);
  color: var(--brand);
  font-size: 12px;
  font-weight: 600;
}

.hero-title {
  margin: 0 0 10px;
  font-size: 26px;
  line-height: 1.35;
}

.hero-desc {
  margin: 0;
  color: var(--text-2);
  font-size: 14px;
  line-height: 1.7;
}

.hero-user {
  align-self: center;
  padding: 18px;
  border: 1px dashed #d7e3f5;
  border-radius: 12px;
  background: #fff;
}

.hero-label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-2);
}

.hero-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 10px;
  background: #f7f9fc;
}

.hero-avatar {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 50%;
  background: #fff;
  font-size: 18px;
}

.hero-user-info strong {
  font-size: 14px;
}

.hero-uid {
  margin: 2px 0 0;
  color: var(--text-3);
  font-size: 12px;
  word-break: break-all;
}

.hero-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--text-3);
}

.pay-tip {
  margin-bottom: 22px;
  padding: 16px 20px;
  border: 1px dashed #d7e3f5;
  background: linear-gradient(135deg, #fbfdff, #f4f9ff);
}

.pay-tip-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 14px;
}

.pay-tip-icon {
  font-size: 16px;
}

.pay-tip-sub {
  color: var(--text-3);
  font-size: 12px;
}

.pay-tip-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.pay-tip-grid > div {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  border-radius: 9px;
  background: #fff;
}

.pay-tip-grid span {
  color: var(--text-3);
  font-size: 12px;
}

.pay-tip-grid b {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 13px;
  word-break: break-all;
}

.list-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 14px;
}

.list-head h2 {
  margin: 0;
  font-size: 18px;
}

.list-count {
  font-size: 13px;
  color: var(--text-3);
}

.alert {
  margin: 0 0 14px;
  padding: 10px 14px;
  border-radius: 10px;
  background: rgba(240, 68, 56, 0.08);
  color: var(--danger);
  font-size: 13px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.product {
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(22, 47, 87, 0.1);
}

.product-thumb {
  display: grid;
  height: 148px;
  place-items: center;
  background: linear-gradient(135deg, #eef4ff, #f8fbff);
  font-size: 54px;
}

.product-body {
  padding: 16px;
}

.product-name {
  margin: 0 0 6px;
  font-size: 16px;
}

.product-desc {
  display: -webkit-box;
  margin: 0 0 16px;
  overflow: hidden;
  color: var(--text-2);
  font-size: 13px;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.product-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price {
  color: var(--danger);
  font-size: 22px;
  font-weight: 700;
}

.price em {
  margin-right: 2px;
  font-size: 14px;
  font-style: normal;
}

@media (max-width: 760px) {
  .pay-tip-grid {
    grid-template-columns: 1fr;
  }

  .hero {
    grid-template-columns: 1fr;
    padding: 22px;
    gap: 20px;
  }

  .hero-title {
    font-size: 21px;
  }
}
</style>
