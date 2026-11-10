<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderStatus } from '../api'
import { formatAmount, formatTime, statusMeta } from '../utils/order'

const route = useRoute()
const router = useRouter()
const orderId = route.params.orderId

const order = ref(null)
const loading = ref(true)
const error = ref('')
const polling = ref(false)
// 等待超过一定时间后，提示“支付宝沙箱可能维护中”
const slowPayment = ref(false)
let slowTimer = null

let timer = null
let attempts = 0
const MAX_ATTEMPTS = 20
const INTERVAL = 3000

const isFinal = (status) => ['PAY_SUCCESS', 'DEAL_DONE', 'CLOSE'].includes(status)

async function fetchStatus(silent = false) {
  if (!silent) loading.value = true
  try {
    order.value = await getOrderStatus(orderId)
    error.value = ''
    if (isFinal(order.value?.orderStatusVO)) {
      slowPayment.value = false
    }
    if (!isFinal(order.value?.orderStatusVO)) {
      startPolling()
    } else {
      stopPolling()
    }
  } catch (e) {
    if (!silent) error.value = e.message
  } finally {
    if (!silent) loading.value = false
  }
}

function startPolling() {
  if (timing() || polling.value) return
  polling.value = true
  attempts = 0
  timer = setInterval(async () => {
    attempts += 1
    if (attempts > MAX_ATTEMPTS) {
      stopPolling()
      return
    }
    await fetchStatus(true)
  }, INTERVAL)
}

function timing() {
  return timer !== null
}

function stopPolling() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  polling.value = false
}

/** 用订单里保存的支付表单重新打开支付宝收银台 */
function reopenPay() {
  const html = order.value && order.value.payUrl
  if (!html) {
    error.value = '这条订单没有可用的支付链接，请返回商城重新下单'
    return
  }
  const payWindow = window.open('', '_blank')
  if (payWindow) {
    payWindow.document.open()
    payWindow.document.write(html)
    payWindow.document.close()
  } else {
    error.value = '浏览器拦截了弹窗，请允许本站弹出窗口后重试'
  }
}

function retryPayment() {
  router.push({ name: 'home' })
}

onMounted(() => {
  fetchStatus()
  // 弹窗被拦截，或等待超过 8 秒还没付成功，都直接给出提示
  if (route.query.blocked === '1') {
    slowPayment.value = true
  } else {
    slowTimer = setTimeout(() => { slowPayment.value = true }, 8000)
  }
})
onBeforeUnmount(() => {
  stopPolling()
  if (slowTimer) clearTimeout(slowTimer)
})
</script>

<template>
  <div class="page">
    <div class="container narrow">
      <div class="card result">
        <template v-if="loading">
          <div class="skeleton" style="height: 84px; width: 84px; border-radius: 50%; margin: 0 auto" />
          <div class="skeleton" style="height: 22px; width: 40%; margin: 20px auto 0" />
          <div class="skeleton" style="height: 14px; width: 60%; margin: 12px auto 0" />
        </template>

        <template v-else-if="error">
          <div class="icon icon-error">!</div>
          <h1 class="result-title">查询失败</h1>
          <p class="result-desc">{{ error }}</p>
          <div class="result-actions">
            <button class="btn btn-ghost" @click="router.push({ name: 'orders' })">查看我的订单</button>
            <button class="btn" @click="fetchStatus()">重新查询</button>
          </div>
        </template>

        <template v-else>
          <div
            class="icon"
            :class="{
              'icon-success': order.orderStatusVO === 'PAY_SUCCESS' || order.orderStatusVO === 'DEAL_DONE',
              'icon-wait': order.orderStatusVO === 'PAY_WAIT' || order.orderStatusVO === 'CREATE',
              'icon-muted': order.orderStatusVO === 'CLOSE',
            }"
          >
            {{ order.orderStatusVO === 'CLOSE' ? '×' : order.orderStatusVO === 'PAY_SUCCESS' ? '✓' : '…' }}
          </div>

          <h1 class="result-title">
            {{
              order.orderStatusVO === 'PAY_SUCCESS' || order.orderStatusVO === 'DEAL_DONE'
                ? '支付成功'
                : order.orderStatusVO === 'CLOSE'
                  ? '订单已关闭'
                  : '等待支付结果'
            }}
          </h1>

          <p class="result-desc">
            {{
              order.orderStatusVO === 'PAY_SUCCESS' || order.orderStatusVO === 'DEAL_DONE'
                ? '支付宝已回调，订单状态已更新'
                : order.orderStatusVO === 'CLOSE'
                  ? '订单超时未支付，已自动关闭'
                  : '已跳转支付宝收银台，完成支付后本页会自动刷新'
            }}
          </p>

          <div
            v-if="slowPayment && order && (order.orderStatusVO === 'PAY_WAIT' || order.orderStatusVO === 'CREATE')"
            class="warn-tip"
          >
            <p class="warn-title">⚠️ 收银台打不开，或提示「系统繁忙 / 504」？</p>
            <p class="warn-desc">
              这不是商城的问题，是<strong>支付宝沙箱收单服务维护中</strong>。可以过一会儿再试，
              或点下面的按钮重新打开支付页。
            </p>
            <button class="btn btn-ghost btn-sm" @click="reopenPay">重新打开支付页</button>
          </div>

          <div v-if="polling" class="sandbox-tip">
            <p class="sandbox-title">沙箱收银台测试账号</p>
            <p>登录账号 <b>orbyqu7212@sandbox.com</b></p>
            <p>登录密码 / 支付密码 <b>111111</b></p>
          </div>

          <div v-if="polling" class="polling">
            <span class="spinner" />
            正在实时查询订单状态（每 3 秒）
          </div>

          <dl class="detail">
            <div>
              <dt>订单号</dt>
              <dd class="mono">{{ order.orderId }}</dd>
            </div>
            <div>
              <dt>商品</dt>
              <dd>{{ order.productName }}</dd>
            </div>
            <div>
              <dt>金额</dt>
              <dd class="amount">¥{{ formatAmount(order.totalAmount) }}</dd>
            </div>
            <div>
              <dt>下单时间</dt>
              <dd>{{ formatTime(order.orderTime) }}</dd>
            </div>
            <div>
              <dt>当前状态</dt>
              <dd>
                <span class="tag" :class="statusMeta(order.orderStatusVO).cls">
                  {{ statusMeta(order.orderStatusVO).text }}
                </span>
              </dd>
            </div>
          </dl>

          <div class="result-actions">
            <button class="btn btn-ghost" @click="router.push({ name: 'orders' })">我的订单</button>
            <button
              v-if="order.orderStatusVO === 'PAY_WAIT' || order.orderStatusVO === 'CREATE'"
              class="btn"
              @click="reopenPay"
            >
              重新打开支付页
            </button>
            <button v-else class="btn" @click="router.push({ name: 'home' })">再买一件</button>
          </div>
        </template>
      </div>

      <p class="hint">支付完成后若状态未更新，可稍等片刻或到「我的订单」手动刷新。</p>
    </div>
  </div>
</template>

<style scoped>
.narrow {
  max-width: 620px;
}

.result {
  padding: 44px 32px 32px;
  text-align: center;
}

.icon {
  display: grid;
  width: 84px;
  height: 84px;
  margin: 0 auto;
  place-items: center;
  border-radius: 50%;
  font-size: 40px;
  font-weight: 700;
  color: #fff;
}

.icon-success {
  background: linear-gradient(135deg, #12b76a, #3ed598);
}

.icon-wait {
  background: linear-gradient(135deg, #f79009, #ffc247);
}

.icon-muted {
  background: linear-gradient(135deg, #98a2b3, #c3cbd8);
}

.icon-error {
  background: linear-gradient(135deg, #f04438, #ff7a70);
}

.result-title {
  margin: 20px 0 8px;
  font-size: 22px;
}

.result-desc {
  margin: 0;
  color: var(--text-2);
  font-size: 14px;
}

.warn-tip {
  margin: 18px auto 0;
  padding: 14px 16px;
  max-width: 420px;
  border: 1px solid rgba(247, 144, 9, 0.35);
  border-radius: 10px;
  background: #fffaf0;
  text-align: left;
}

.warn-title {
  margin: 0 0 6px;
  font-size: 13px;
  font-weight: 700;
  color: #b45309;
}

.warn-desc {
  margin: 0 0 10px;
  font-size: 12.5px;
  line-height: 1.6;
  color: var(--text-2);
}

.warn-desc strong {
  color: #b45309;
}

.sandbox-tip {
  margin: 18px auto 0;
  padding: 12px 16px;
  max-width: 360px;
  border: 1px dashed #d7e3f5;
  border-radius: 10px;
  background: #f8fbff;
  text-align: left;
  font-size: 12px;
  color: var(--text-2);
}

.sandbox-title {
  margin: 0 0 6px;
  font-weight: 600;
  color: var(--text);
}

.sandbox-tip p {
  margin: 3px 0;
}

.sandbox-tip b {
  font-family: 'SFMono-Regular', Consolas, monospace;
  color: var(--brand);
}

.polling {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 6px 14px;
  border-radius: 999px;
  background: #f2f7ff;
  color: var(--brand);
  font-size: 12px;
}

.spinner {
  width: 12px;
  height: 12px;
  border: 2px solid rgba(22, 119, 255, 0.25);
  border-top-color: var(--brand);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.detail {
  margin: 28px 0 0;
  padding: 18px 20px;
  border-radius: 12px;
  background: #f8fafd;
  text-align: left;
}

.detail > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.detail > div + div {
  border-top: 1px dashed #e4eaf3;
}

.detail dt {
  color: var(--text-3);
}

.detail dd {
  margin: 0;
  text-align: right;
}

.mono {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 13px;
}

.amount {
  color: var(--danger);
  font-weight: 700;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 28px;
}

.hint {
  margin: 18px 0 0;
  text-align: center;
  color: var(--text-3);
  font-size: 12px;
}
</style>
