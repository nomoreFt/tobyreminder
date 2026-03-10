'use client'

import React, { createContext, useCallback, useContext, useRef, useState } from 'react'
import * as api from '@/lib/api'
import type { Reminder, ReminderList, ReminderListRequest, ReminderRequest, SmartFilter } from '@/types'

type SelectedId = number | SmartFilter | null

export interface SmartCounts {
  today: number
  scheduled: number
  flagged: number
}

interface AppState {
  lists: ReminderList[]
  reminders: Reminder[]
  selectedId: SelectedId
  selectedReminderId: number | null
  smartCounts: SmartCounts
}

interface AppActions {
  fetchLists: () => Promise<void>
  createList: (data: ReminderListRequest) => Promise<void>
  updateList: (id: number, data: ReminderListRequest) => Promise<void>
  deleteList: (id: number) => Promise<void>
  reorderLists: (ids: number[]) => Promise<void>
  selectList: (id: SelectedId) => Promise<void>
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
  const [smartCounts, setSmartCounts] = useState<SmartCounts>({ today: 0, scheduled: 0, flagged: 0 })

  // 롤백용 최신 상태 참조
  const listsRef = useRef(lists)
  listsRef.current = lists
  const remindersRef = useRef(reminders)
  remindersRef.current = reminders
  const selectedIdRef = useRef(selectedId)
  selectedIdRef.current = selectedId

  const fetchLists = useCallback(async () => {
    const [lists, todayRes, scheduledRes, flaggedRes] = await Promise.all([
      api.getLists(),
      api.getRemindersByFilter('today'),
      api.getRemindersByFilter('scheduled'),
      api.getRemindersByFilter('flagged'),
    ])
    setLists(lists)
    setSmartCounts({ today: todayRes.length, scheduled: scheduledRes.length, flagged: flaggedRes.length })
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
    setSelectedId(prev => {
      if (prev === id) {
        setReminders([])
        return null
      }
      return prev
    })
  }, [])

  const reorderLists = useCallback(async (ids: number[]) => {
    const snapshot = listsRef.current
    const reordered = ids.map(id => snapshot.find(l => l.id === id)!).filter(Boolean)
    setLists(reordered)
    try {
      await api.reorderLists(ids)
    } catch (err) {
      setLists(snapshot)
      throw err
    }
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
    const currentId = selectedIdRef.current
    if (typeof currentId === 'number' && currentId === listId) {
      setReminders(await api.getRemindersByList(listId))
    } else if (typeof currentId === 'string') {
      setReminders(await api.getRemindersByFilter(currentId as SmartFilter))
    }
    setLists(await api.getLists())
  }, [])

  const updateReminder = useCallback(async (id: number, data: ReminderRequest) => {
    const updated = await api.updateReminder(id, data)
    setReminders(prev => prev.map(r => r.id === id ? updated : r))
  }, [])

  const deleteReminder = useCallback(async (id: number) => {
    await api.deleteReminder(id)
    setReminders(prev => prev.filter(r => r.id !== id))
    setSelectedReminderId(prev => prev === id ? null : prev)
    setLists(await api.getLists())
  }, [])

  const toggleComplete = useCallback(async (id: number) => {
    const snapshot = remindersRef.current.find(r => r.id === id)
    setReminders(prev => prev.filter(r => r.id !== id))
    setSelectedReminderId(prev => prev === id ? null : prev)
    try {
      await api.toggleComplete(id)
      setLists(await api.getLists())
    } catch (err) {
      if (snapshot) {
        setReminders(prev => [...prev, snapshot].sort((a, b) => a.sortOrder - b.sortOrder))
      }
      throw err
    }
  }, [])

  const reorderReminders = useCallback(async (listId: number, ids: number[]) => {
    const snapshot = remindersRef.current
    const reordered = ids.map(id => snapshot.find(r => r.id === id)!).filter(Boolean)
    setReminders(reordered)
    try {
      await api.reorderReminders(listId, ids)
    } catch (err) {
      setReminders(snapshot)
      throw err
    }
  }, [])

  const selectReminder = useCallback((id: number | null) => {
    setSelectedReminderId(id)
  }, [])

  return (
    <AppContext.Provider value={{
      lists, reminders, selectedId, selectedReminderId, smartCounts,
      fetchLists, createList, updateList, deleteList, reorderLists, selectList,
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
