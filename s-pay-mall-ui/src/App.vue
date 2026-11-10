<script setup>
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { computed } from 'vue'
import { clearUser, user } from './stores/user'

const route = useRoute()
const router = useRouter()
const navList = [
  { name: 'home', label: '商城' },
  { name: 'orders', label: '我的订单' },
]
const activeName = computed(() => (route.name === 'result' ? 'orders' : route.name))

function logout() {
  clearUser()
  router.push({ name: 'home' })
}
</script>

<template>
  <div class="app">
    <header class="header">
      <div class="container header-inner">
        <RouterLink to="/" class="brand">
          <span class="brand-logo">支</span>
          <span class="brand-text">
            <strong>模拟支付商城</strong>
            <em>MVC 分层架构演示</em>
          </span>
        </RouterLink>

        <div class="header-right">
          <nav class="nav">
            <RouterLink
              v-for="item in navList"
              :key="item.name"
              :to="{ name: item.name }"
              class="nav-link"
              :class="{ active: activeName === item.name }"
            >
              {{ item.label }}
            </RouterLink>
          </nav>

          <div class="user-slot">
            <a class="author-link" href="https://dabuw.online" target="_blank" rel="noopener">
              <span>🏠</span> 作者主页
            </a>
            <template v-if="user.userId">
              <span class="user-chip">
                <span class="avatar">{{ user.guest ? '👤' : '💬' }}</span>
                {{ user.nickname || user.userId }}
              </span>
              <button class="logout" @click="logout">退出</button>
            </template>
            <RouterLink v-else :to="{ name: 'login' }" class="btn btn-sm">微信登录</RouterLink>
          </div>
        </div>
      </div>
    </header>

    <main class="main">
      <RouterView v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </RouterView>
    </main>

    <footer class="footer">
      <div class="container">
        <p>模拟支付商城 · Spring Boot + MVC 分层 + 微信扫码登录 + 支付宝沙箱支付</p>
        <p class="footer-sub">本项目仅用于技术学习与演示，不涉及真实交易</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.app {
  display: flex;
  min-height: 100%;
  flex-direction: column;
}

.header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: saturate(180%) blur(12px);
  border-bottom: 1px solid var(--line);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 62px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-logo {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 10px;
  background: linear-gradient(135deg, #1677ff, #4f9bff);
  color: #fff;
  font-weight: 700;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.brand-text strong {
  font-size: 15px;
}

.brand-text em {
  font-style: normal;
  font-size: 11px;
  color: var(--text-3);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav {
  display: flex;
  gap: 6px;
}

.user-slot {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid var(--line);
  border-radius: 9px;
  background: #fff;
  color: var(--text-2);
  font-size: 13px;
  transition: all 0.18s ease;
}

.author-link:hover {
  border-color: #c9d6ea;
  background: #f7f9fc;
  color: var(--text);
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px 5px 6px;
  border-radius: 999px;
  background: #f2f6fc;
  font-size: 13px;
  color: var(--text-2);
}

.avatar {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  border-radius: 50%;
  background: #fff;
  font-size: 13px;
}

.logout {
  border: 0;
  background: transparent;
  color: var(--text-3);
  font-size: 12px;
  cursor: pointer;
}

.logout:hover {
  color: var(--danger);
}

.nav-link {
  padding: 7px 14px;
  border-radius: 9px;
  font-size: 14px;
  color: var(--text-2);
  transition: all 0.18s ease;
}

.nav-link:hover {
  background: #f1f5fb;
  color: var(--text);
}

.nav-link.active {
  background: rgba(22, 119, 255, 0.1);
  color: var(--brand);
  font-weight: 600;
}

.main {
  flex: 1;
}

.footer {
  padding: 26px 0;
  border-top: 1px solid var(--line);
  background: #fff;
  text-align: center;
  color: var(--text-3);
  font-size: 13px;
}

.footer p {
  margin: 0;
}

.footer-sub {
  margin-top: 6px !important;
  font-size: 12px;
  color: #aab3c2;
}
</style>
