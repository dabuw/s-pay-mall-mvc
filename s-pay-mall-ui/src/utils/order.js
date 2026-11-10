export const ORDER_STATUS = {
  CREATE: { text: '待支付', cls: 'tag-warning' },
  PAY_WAIT: { text: '待支付', cls: 'tag-warning' },
  PAY_SUCCESS: { text: '支付成功', cls: 'tag-success' },
  DEAL_DONE: { text: '交易完成', cls: 'tag-success' },
  CLOSE: { text: '已关闭', cls: 'tag-muted' },
}

export function statusMeta(status) {
  return ORDER_STATUS[status] || { text: status || '未知', cls: 'tag-muted' }
}

export function formatTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return '-'
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(
    d.getMinutes(),
  )}`
}

export function formatAmount(value) {
  const num = Number(value)
  return Number.isFinite(num) ? num.toFixed(2) : '-'
}

const USER_KEY = 'spay_user_id'

export function getUserId() {
  return localStorage.getItem(USER_KEY) || 'liujiani'
}

export function setUserId(userId) {
  localStorage.setItem(USER_KEY, userId)
}
