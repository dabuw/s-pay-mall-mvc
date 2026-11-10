<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { checkLogin, getQrCodeTicket } from '../api'
import { loginAsGuest, setUser } from '../stores/user'

const router = useRouter()
const route = useRoute()

const ticket = ref('')
const qrcodeUrl = ref('')
const loading = ref(true)
const error = ref('')
const expired = ref(false)
const seconds = ref(300)

let pollTimer = null
let countdownTimer = null

const redirect = () => route.query.redirect || '/'

function stopTimers() {
  if (pollTimer) clearInterval(pollTimer)
  if (countdownTimer) clearInterval(countdownTimer)
  pollTimer = null
  countdownTimer = null
}

async function loadQrCode() {
  stopTimers()
  loading.value = true
  expired.value = false
  error.value = ''
  seconds.value = 300

  try {
    const ticketValue = await getQrCodeTicket()
    if (!ticketValue) throw new Error('二维码生成失败，请检查公众号配置')
    ticket.value = ticketValue
    qrcodeUrl.value = `https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=${encodeURIComponent(ticketValue)}`
    startPolling()
    startCountdown()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function startPolling() {
  pollTimer = setInterval(async () => {
    if (!ticket.value) return
    try {
      const res = await checkLogin(ticket.value)
      if (res && res.code === '0000' && res.data) {
        stopTimers()
        setUser({ userId: res.data, nickname: '微信用户' })
        router.replace(redirect())
      }
    } catch {
      // 轮询失败静默处理，等待下一次
    }
  }, 2000)
}

function startCountdown() {
  countdownTimer = setInterval(() => {
    seconds.value -= 1
    if (seconds.value <= 0) {
      stopTimers()
      expired.value = true
    }
  }, 1000)
}

function guestLogin() {
  loginAsGuest()
  router.replace(redirect())
}

onMounted(loadQrCode)
onBeforeUnmount(stopTimers)
</script>

<template>
  <div class="page">
    <div class="container narrow">
      <div class="card login-card">
        <h1 class="login-title">微信扫码登录</h1>
        <p class="login-desc">使用微信扫描二维码，即可完成登录并绑定你的订单</p>

        <div class="qr-wrap">
          <div v-if="loading" class="qr skeleton" />
          <div v-else-if="error" class="qr qr-error">
            <span>二维码加载失败</span>
            <p>{{ error }}</p>
          </div>
          <div v-else-if="expired" class="qr qr-error">
            <span>二维码已过期</span>
            <button class="btn btn-sm" style="margin-top: 12px" @click="loadQrCode">刷新二维码</button>
          </div>
          <img v-else :src="qrcodeUrl" class="qr" alt="微信登录二维码" />
        </div>

        <p v-if="!loading && !error && !expired" class="countdown">
          二维码有效期 {{ Math.floor(seconds / 60) }}:{{ String(seconds % 60).padStart(2, '0') }}
        </p>

        <div class="divider"><span>或者</span></div>

        <button class="btn btn-ghost guest-btn" @click="guestLogin">游客身份进入（无需扫码）</button>
        <p class="tip">游客模式下订单归属随机 ID，仅用于体验支付流程</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.narrow {
  max-width: 460px;
}

.login-card {
  padding: 36px 32px 30px;
  text-align: center;
}

.login-title {
  margin: 0 0 8px;
  font-size: 21px;
}

.login-desc {
  margin: 0 0 22px;
  color: var(--text-2);
  font-size: 13px;
}

.qr-wrap {
  display: flex;
  justify-content: center;
}

.qr {
  display: grid;
  width: 220px;
  height: 220px;
  place-items: center;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fff;
  object-fit: contain;
  font-size: 13px;
  color: var(--text-3);
}

.qr-error {
  padding: 16px;
  text-align: center;
  line-height: 1.6;
}

.countdown {
  margin: 14px 0 0;
  color: var(--text-3);
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.divider {
  position: relative;
  margin: 24px 0 16px;
  color: var(--text-3);
  font-size: 12px;
}

.divider::before {
  position: absolute;
  top: 50%;
  right: 0;
  left: 0;
  height: 1px;
  background: var(--line);
  content: '';
}

.divider span {
  position: relative;
  padding: 0 10px;
  background: #fff;
}

.guest-btn {
  width: 100%;
}

.tip {
  margin: 12px 0 0;
  color: var(--text-3);
  font-size: 12px;
}
</style>
