import type { Reminder, ReminderList, ReminderListRequest, ReminderRequest, SmartFilter } from '@/types'

const BASE = 'http://localhost:8080'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...init,
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }))
    throw new Error(err.message ?? res.statusText)
  }
  if (res.status === 204) return undefined as T
  return res.json()
}

// ReminderList
export const getLists = (): Promise<ReminderList[]> => request('/api/lists')

export const createList = (data: ReminderListRequest): Promise<ReminderList> =>
  request('/api/lists', { method: 'POST', body: JSON.stringify(data) })

export const updateList = (id: number, data: ReminderListRequest): Promise<ReminderList> =>
  request(`/api/lists/${id}`, { method: 'PUT', body: JSON.stringify(data) })

export const deleteList = (id: number): Promise<void> =>
  request(`/api/lists/${id}`, { method: 'DELETE' })

export const reorderLists = (ids: number[]): Promise<void> =>
  request('/api/lists/reorder', { method: 'PATCH', body: JSON.stringify({ ids }) })

// Reminder
export const getRemindersByList = (listId: number): Promise<Reminder[]> =>
  request(`/api/lists/${listId}/reminders`)

export const getRemindersByFilter = (filter: SmartFilter): Promise<Reminder[]> =>
  request(`/api/reminders?filter=${filter}`)

export const createReminder = (listId: number, data: ReminderRequest): Promise<Reminder> =>
  request(`/api/lists/${listId}/reminders`, { method: 'POST', body: JSON.stringify(data) })

export const updateReminder = (id: number, data: ReminderRequest): Promise<Reminder> =>
  request(`/api/reminders/${id}`, { method: 'PUT', body: JSON.stringify(data) })

export const deleteReminder = (id: number): Promise<void> =>
  request(`/api/reminders/${id}`, { method: 'DELETE' })

export const toggleComplete = (id: number): Promise<Reminder> =>
  request(`/api/reminders/${id}/complete`, { method: 'PATCH' })

export const reorderReminders = (listId: number, ids: number[]): Promise<void> =>
  request(`/api/lists/${listId}/reminders/reorder`, { method: 'PATCH', body: JSON.stringify({ ids }) })
