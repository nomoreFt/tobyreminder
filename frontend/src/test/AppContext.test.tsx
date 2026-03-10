import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import React from 'react'
import { AppProvider, useApp } from '@/context/AppContext'
import * as api from '@/lib/api'
import type { Reminder, ReminderList } from '@/types'

vi.mock('@/lib/api')

const mockList: ReminderList = {
  id: 1, name: '테스트', color: '#007AFF', icon: 'list.bullet',
  sortOrder: 0, reminderCount: 2, createdAt: '2026-01-01T00:00:00',
}

const mockReminder = (id: number): Reminder => ({
  id, listId: 1, title: `리마인더 ${id}`, notes: null,
  dueDate: null, dueTime: null, priority: 'NONE',
  isFlagged: false, isCompleted: false, completedAt: null,
  sortOrder: id - 1, createdAt: '2026-01-01T00:00:00',
})

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <AppProvider>{children}</AppProvider>
)

describe('AppContext', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(api.getLists).mockResolvedValue([mockList])
    vi.mocked(api.getRemindersByList).mockResolvedValue([mockReminder(1), mockReminder(2)])
    vi.mocked(api.getRemindersByFilter).mockResolvedValue([])
  })

  describe('toggleComplete()', () => {
    it('API 성공 시 reminders에서 즉시 제거된다', async () => {
      vi.mocked(api.toggleComplete).mockResolvedValue({ ...mockReminder(1), isCompleted: true })
      vi.mocked(api.getLists).mockResolvedValue([{ ...mockList, reminderCount: 1 }])

      const { result } = renderHook(() => useApp(), { wrapper })

      await act(async () => {
        await result.current.fetchLists()
        await result.current.selectList(1)
      })
      expect(result.current.reminders).toHaveLength(2)

      await act(async () => {
        await result.current.toggleComplete(1)
      })
      expect(result.current.reminders).toHaveLength(1)
      expect(result.current.reminders[0].id).toBe(2)
    })

    it('API 실패 시 제거한 reminder를 복원한다', async () => {
      vi.mocked(api.toggleComplete).mockRejectedValue(new Error('Network error'))

      const { result } = renderHook(() => useApp(), { wrapper })

      await act(async () => {
        await result.current.fetchLists()
        await result.current.selectList(1)
      })
      expect(result.current.reminders).toHaveLength(2)

      await act(async () => {
        try { await result.current.toggleComplete(1) } catch { /* expected */ }
      })

      // 실패 후 복원되어야 한다
      expect(result.current.reminders).toHaveLength(2)
      expect(result.current.reminders.some(r => r.id === 1)).toBe(true)
    })
  })

  describe('reorderLists()', () => {
    it('API 실패 시 lists 순서를 원복한다', async () => {
      const list2: ReminderList = { ...mockList, id: 2, name: '목록2', sortOrder: 1 }
      vi.mocked(api.getLists).mockResolvedValue([mockList, list2])
      vi.mocked(api.reorderLists).mockRejectedValue(new Error('Network error'))

      const { result } = renderHook(() => useApp(), { wrapper })
      await act(async () => { await result.current.fetchLists() })

      const originalOrder = result.current.lists.map(l => l.id)

      await act(async () => {
        try { await result.current.reorderLists([2, 1]) } catch { /* expected */ }
      })

      // 실패 후 원래 순서로 복원되어야 한다
      expect(result.current.lists.map(l => l.id)).toEqual(originalOrder)
    })
  })
})
