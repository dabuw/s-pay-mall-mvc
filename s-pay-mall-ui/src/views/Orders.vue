<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList } from '../api'
import { formatAmount, formatTime, statusMeta } from '../utils/order'
import { user } from '../stores/user'

const router = useRouter()
const orders = ref([])
const loading = ref(true)
const error = ref('')
const filter = ref('ALL')

const filters = [
  { key: 'ALL', label: '全部' },
  { key: 'PAY_WAIT', label: '待支付' },
  { key: 'PAY_SUCCESS', label: '已支付' },
  { key: 'CLOSE', label: '已关闭' },
]

async function load() {
  loading.value = true
  error.value = ''
  try {
    orders.value = (await getOrderList(user.userId)) || []
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function visibleOrders() {
  if (filter.value === 'ALL') return orders.value
  if (filter.value === 'PAY_SUCCESS') {
    return orders.value.filter((o) => ['PAY_SUCCESS', 'DEAL_DONE'].includes(o.orderStatusVO))
  }
  return orders.value.filter((o) => o.orderStatusVO === filter.value)
}

function goDetail(orderId) {
  router.push({ name: 'result', params: { orderId } })
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="container">
      <div class="head">
        <div>
          <h1 class="page-title">我的订单</h1>
          <p class="page-subtitle">演示环境按用户 ID 隔离订单，可随时切换查看</p>
        </div>
        <div class="head-actions">
          <span class="user-chip">
            <span class="avatar">{{ user.guest ? '👤' : '💬' }}</span>
            {{ user.nickname || '微信用户' }}
          </span>
          <button class="btn btn-ghost btn-sm" @click="load">刷新</button>
          <button class="btn btn-sm" @click="router.push({ name: 'home' })">去下单</button>
        </div>
      </div>

      <div class="tabs">
        <button
          v-for="item in filters"
          :key="item.key"
          class="tab"
          :class="{ active: filter === item.key }"
          @click="filter = item.key"
        >
          {{ item.label }}
        </button>
      </div>

      <p v-if="error" class="alert">{{ error }}</p>

      <div v-if="loading" class="card order-list">
        <div v-for="n in 3" :key="n" class="order-row">
          <div class="skeleton" style="height: 16px; width: 30%" />
          <div class="skeleton" style="height: 16px; width: 20%" />
        </div>
      </div>

      <div v-else-if="visibleOrders().length" class="card order-list">
        <button v-for="order in visibleOrders()" :key="order.orderId" class="order-row" @click="goDetail(order.orderId)">
          <div class="order-main">
            <div class="order-name">
              {{ order.productName }}
              <span class="tag" :class="statusMeta(order.orderStatusVO).cls">
                {{ statusMeta(order.orderStatusVO).text }}
              </span>
            </div>
            <p class="order-meta">
              订单号 {{ order.orderId }} · {{ formatTime(order.orderTime) }}
            </p>
          </div>
          <div class="order-side">
            <span class="amount">¥{{ formatAmount(order.totalAmount) }}</span>
            <span class="arrow">›</span>
          </div>
        </button>
      </div>

      <div v-else class="card empty">
        <div class="empty-icon">🧾</div>
        <p>还没有订单，去商城逛逛吧</p>
        <button class="btn" style="margin-top: 16px" @click="router.push({ name: 'home' })">去下单</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.head-actions {
  display: flex;
  gap: 8px;
}

.tabs {
  display: flex;
  gap: 6px;
  margin-bottom: 16px;
}

.tab {
  padding: 7px 14px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: var(--text-2);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.tab:hover {
  border-color: #c9d6ea;
}

.tab.active {
  border-color: var(--brand);
  background: rgba(22, 119, 255, 0.08);
  color: var(--brand);
  font-weight: 600;
}

.alert {
  margin: 0 0 14px;
  padding: 10px 14px;
  border-radius: 10px;
  background: rgba(240, 68, 56, 0.08);
  color: var(--danger);
  font-size: 13px;
}

.order-list {
  overflow: hidden;
}

.order-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 16px;
  padding: 18px 20px;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: background 0.16s ease;
}

.order-row + .order-row {
  border-top: 1px solid var(--line);
}

.order-row:hover {
  background: #f8fafd;
}

.order-name {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
}

.order-meta {
  margin: 8px 0 0;
  color: var(--text-3);
  font-size: 12px;
}

.order-side {
  display: flex;
  align-items: center;
  gap: 12px;
  white-space: nowrap;
}

.amount {
  color: var(--danger);
  font-weight: 700;
  font-size: 16px;
}

.arrow {
  color: var(--text-3);
  font-size: 20px;
}

@media (max-width: 640px) {
  .order-meta {
    max-width: 180px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
