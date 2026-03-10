import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import React from 'react'
import { AppProvider, useApp } from '@/context/AppContext'
import * as api from '@/lib/api'
import type { Reminder, ReminderList } from '@/types'

vi.mock('@/lib/api')

const mockList: ReminderList = {
  id: 1, name: '테스트', color: '#007AFF', icon: 'list.bullet',
  sortOrder: 0, reminderCount: 5, createdAt: '2026-01-01T00:00:00',
}

const makeReminder = (id: number): Reminder => ({
  id, listId: 1, title: `리마인더 ${id}`, notes: null,
  dueDate: null, dueTime: null, priority: 'NONE',
  isFlagged: false, isCompleted: false, completedAt: null,
  sortOrder: id - 1, createdAt: '2026-01-01T00:00:00',
})

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <AppProvider>{children}</AppProvider>
)

describe('SmartLists 배지 카운트', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(api.getLists).mockResolvedValue([mockList])
    vi.mocked(api.getRemindersByFilter).mockImplementation((filter) => {
      if (filter === 'today') return Promise.resolve([makeReminder(1), makeReminder(2)])
      if (filter === 'flagged') return Promise.resolve([makeReminder(3)])
      if (filter === 'scheduled') return Promise.resolve([makeReminder(4), makeReminder(5), makeReminder(6)])
      return Promise.resolve([])
    })
  })

  it('fetchLists 호출 후 오늘 카운트가 API 결과를 반영한다', async () => {
    const { result } = renderHook(() => useApp(), { wrapper })

    await act(async () => { await result.current.fetchLists() })

    // smartCounts.today가 2여야 한다
    expect(result.current.smartCounts.today).toBe(2)
  })

  it('fetchLists 호출 후 플래그됨 카운트가 API 결과를 반영한다', async () => {
    const { result } = renderHook(() => useApp(), { wrapper })

    await act(async () => { await result.current.fetchLists() })

    expect(result.current.smartCounts.flagged).toBe(1)
  })

  it('fetchLists 호출 후 예정 카운트가 API 결과를 반영한다', async () => {
    const { result } = renderHook(() => useApp(), { wrapper })

    await act(async () => { await result.current.fetchLists() })

    expect(result.current.smartCounts.scheduled).toBe(3)
  })
})
