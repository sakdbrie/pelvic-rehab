import { ref, computed } from 'vue'

/**
 * 列表页通用分页逻辑（原型 01/04/07 系列页面均为此模式：筛选 + 分页 + 回顶）
 */
export function useTablePage<T>(source: () => T[], filter: (row: T) => boolean, pageSize = 30) {
  const currentPage = ref(1)

  const filtered = computed(() => source().filter(filter))
  const total = computed(() => filtered.value.length)
  const totalPages = computed(() => Math.ceil(total.value / pageSize) || 1)

  const pagedRows = computed(() => {
    let page = currentPage.value
    if (page > totalPages.value) page = totalPages.value
    if (page < 1) page = 1
    const start = (page - 1) * pageSize
    return filtered.value.slice(start, start + pageSize)
  })

  function resetPage() {
    currentPage.value = 1
  }

  return { currentPage, total, totalPages, pagedRows, resetPage }
}
