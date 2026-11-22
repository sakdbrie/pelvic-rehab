import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types'

/**
 * 用户状态：token、用户信息、权限码
 * TODO: 联调后端时登录成功后写入真实用户信息与权限码
 */
export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)

  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  function setUserInfo(info: UserInfo | null) {
    userInfo.value = info
  }

  function hasPermission(code: string) {
    return userInfo.value?.permissions?.includes(code) ?? false
  }

  return { userInfo, isAdmin, setUserInfo, hasPermission }
})
