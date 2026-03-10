'use client'

import React, { createContext, useCallback, useContext, useState } from 'react'
import * as api from '@/lib/api'
import type { Reminder, ReminderList, ReminderListRequest, ReminderRequest, SmartFilter } from '@/types'

type SelectedId = number | SmartFilter | null

interface AppState {
  lists: ReminderList[]
  reminders: Reminder[]
  selectedId: SelectedId
  selectedReminderId: number | null
}

interface AppActions {
  fetchLists: () => Promise<void>
  createList: (data: ReminderListRequest) => Promise<void>
  updateList: (id: number, data: ReminderListRequest) => Promise<void>
  deleteList: (id: number) => Promise<void>
  reorderLists: (ids: number[]) => Promise<void>
  selectList: (id: SelectedId) => Promise<void>
  setLists: React.Dispatch<React.SetStateAction<ReminderList[]>>
  setReminders: React.Dispatch<React.SetStateAction<Reminder[]>>
  createReminder: (listId: number, data: ReminderRequest) => Promise<void>
  updateReminder: (id: number, data: ReminderRequest) => Promise<void>
  deleteReminder: (id: number) => Promise<void>
  toggleComplete: (id: number) => Promise<void>
  reorderReminders: (listId: number, ids: number[]) => Promise<void>
  selectReminder: (id: number | null) => void
}

const AppContext = createContext<AppState & AppActions | null>(null)

export function AppProvider({ children }: { children: React.ReactNode }) {
  const [lists, setLists] = useState<ReminderList[]>([])
  const [reminders, setReminders] = useState<Reminder[]>([])
  const [selectedId, setSelectedId] = useState<SelectedId>(null)
  const [selectedReminderId, setSelectedReminderId] = useState<number | null>(null)

  const fetchLists = useCallback(async () => {
    setLists(await api.getLists())
  }, [])

  const createList = useCallback(async (data: ReminderListRequest) => {
    await api.createList(data)
    setLists(await api.getLists())
  }, [])

  const updateList = useCallback(async (id: number, data: ReminderListRequest) => {
    await api.updateList(id, data)
    setLists(await api.getLists())
  }, [])

  const deleteList = useCallback(async (id: number) => {
    await api.deleteList(id)
    setLists(await api.getLists())
    if (selectedId === id) {
      setSelectedId(null)
      setReminders([])
    }
  }, [selectedId])

  const reorderLists = useCallback(async (ids: number[]) => {
    await api.reorderLists(ids)
    setLists(await api.getLists())
  }, [])

  const selectList = useCallback(async (id: SelectedId) => {
    setSelectedId(id)
    setSelectedReminderId(null)
    if (id === null) {
      setReminders([])
    } else if (typeof id === 'number') {
      setReminders(await api.getRemindersByList(id))
    } else {
      setReminders(await api.getRemindersByFilter(id as SmartFilter))
    }
  }, [])

  const createReminder = useCallback(async (listId: number, data: ReminderRequest) => {
    await api.createReminder(listId, data)
    if (typeof selectedId === 'number' && selectedId === listId) {
      setReminders(await api.getRemindersByList(listId))
    } else if (typeof selectedId === 'string') {
      setReminders(await api.getRemindersByFilter(selectedId as SmartFilter))
    }
    setLists(await api.getLists())
  }, [selectedId])

  const updateReminder = useCallback(async (id: number, data: ReminderRequest) => {
    const updated = await api.updateReminder(id, data)
    setReminders(prev => prev.map(r => r.id === id ? updated : r))
  }, [])

  const deleteReminder = useCallback(async (id: number) => {
    await api.deleteReminder(id)
    setReminders(prev => prev.filter(r => r.id !== id))
    if (selectedReminderId === id) setSelectedReminderId(null)
    setLists(await api.getLists())
  }, [selectedReminderId])

  const toggleComplete = useCallback(async (id: number) => {
    // 낙관적 업데이트: 즉시 목록에서 제거
    setReminders(prev => prev.filter(r => r.id !== id))
    if (selectedReminderId === id) setSelectedReminderId(null)
    await api.toggleComplete(id)
    setLists(await api.getLists())
  }, [selectedReminderId])

  const reorderReminders = useCallback(async (listId: number, ids: number[]) => {
    await api.reorderReminders(listId, ids)
  }, [])

  const selectReminder = useCallback((id: number | null) => {
    setSelectedReminderId(id)
  }, [])

  return (
    <AppContext.Provider value={{
      lists, reminders, selectedId, selectedReminderId,
      fetchLists, createList, updateList, deleteList, reorderLists, selectList,
      setLists, setReminders,
      createReminder, updateReminder, deleteReminder, toggleComplete, reorderReminders,
      selectReminder,
    }}>
      {children}
    </AppContext.Provider>
  )
}

export function useApp() {
  const ctx = useContext(AppContext)
  if (!ctx) throw new Error('useApp must be used within AppProvider')
  return ctx
}
