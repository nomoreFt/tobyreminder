import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import React from 'react'
import { AppProvider, useApp } from '@/context/AppContext'
import * as api from '@/lib/api'
import type { Reminder, ReminderList } from '@/types'

vi.mock('@/lib/api')

const mockList = (id: number, count: number): ReminderList => ({
  id, name: `목록${id}`, color: '#007AFF', icon: 'list.bullet',
  sortOrder: id - 1, reminderCount: count, createdAt: '2026-01-01T00:00:00',
})

const mockReminder = (id: number, listId = 1): Reminder => ({
  id, listId, title: `리마인더 ${id}`, notes: null,
  dueDate: null, dueTime: null, priority: 'NONE',
  isFlagged: false, isCompleted: false, completedAt: null,
  sortOrder: id - 1, createdAt: '2026-01-01T00:00:00',
})

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <AppProvider>{children}</AppProvider>
)

describe('AppContext reminderCount 로컬 업데이트', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(api.getLists).mockResolvedValue([mockList(1, 2)])
    vi.mocked(api.getRemindersByFilter).mockResolvedValue([])
    vi.mocked(api.getRemindersByList).mockResolvedValue([mockReminder(1), mockReminder(2)])
  })

  it('deleteReminder 후 getLists를 재호출하지 않고 로컬 카운트를 감소시킨다', async () => {
    vi.mocked(api.deleteReminder).mockResolvedValue(undefined)

    const { result } = renderHook(() => useApp(), { wrapper })
    await act(async () => {
      await result.current.fetchLists()
      await result.current.selectList(1)
    })

    const callsBefore = vi.mocked(api.getLists).mock.calls.length

    await act(async () => {
      await result.current.deleteReminder(1)
    })

    // getLists를 추가 호출하지 않아야 한다
    expect(vi.mocked(api.getLists).mock.calls.length).toBe(callsBefore)
    // 카운트가 1 감소해야 한다
    expect(result.current.lists[0].reminderCount).toBe(1)
  })

  it('createReminder 후 getLists를 재호출하지 않고 로컬 카운트를 증가시킨다', async () => {
    vi.mocked(api.createReminder).mockResolvedValue(mockReminder(3))

    const { result } = renderHook(() => useApp(), { wrapper })
    await act(async () => {
      await result.current.fetchLists()
      await result.current.selectList(1)
    })

    const callsBefore = vi.mocked(api.getLists).mock.calls.length

    await act(async () => {
      await result.current.createReminder(1, { title: '새 리마인더' })
    })

    expect(vi.mocked(api.getLists).mock.calls.length).toBe(callsBefore)
    expect(result.current.lists[0].reminderCount).toBe(3)
  })
})
