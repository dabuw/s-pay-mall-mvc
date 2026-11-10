import { reactive } from 'vue'

const STORAGE_KEY = 'spay_user'

function read() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null')
  } catch {
    return null
  }
}

const saved = read()

export const user = reactive({
  userId: saved?.userId || '',
  nickname: saved?.nickname || '',
  guest: saved?.guest || false,
})

export function isLoggedIn() {
  return !!user.userId
}

export function setUser({ userId, nickname = '', guest = false }) {
  user.userId = userId
  user.nickname = nickname
  user.guest = guest
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ userId, nickname, guest }))
}

export function clearUser() {
  user.userId = ''
  user.nickname = ''
  user.guest = false
  localStorage.removeItem(STORAGE_KEY)
}

export function loginAsGuest() {
  const guestId = `guest-${Math.random().toString(36).slice(2, 8)}`
  setUser({ userId: guestId, nickname: '游客', guest: true })
  return guestId
}
