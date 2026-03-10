export type Priority = 'NONE' | 'LOW' | 'MEDIUM' | 'HIGH'

export type SmartFilter = 'today' | 'scheduled' | 'all' | 'flagged' | 'completed'

const SMART_FILTERS: readonly SmartFilter[] = ['today', 'scheduled', 'all', 'flagged', 'completed']
export const isSmartFilter = (v: unknown): v is SmartFilter =>
  typeof v === 'string' && (SMART_FILTERS as readonly string[]).includes(v)

export interface ReminderList {
  id: number
  name: string
  color: string
  icon: string
  sortOrder: number
  reminderCount: number
  createdAt: string
}

export interface Reminder {
  id: number
  listId: number
  title: string
  notes: string | null
  dueDate: string | null
  dueTime: string | null
  priority: Priority
  isFlagged: boolean
  isCompleted: boolean
  completedAt: string | null
  sortOrder: number
  createdAt: string
}

export interface ReminderListRequest {
  name: string
  color?: string
  icon?: string
}

export interface ReminderRequest {
  title: string
  notes?: string | null
  dueDate?: string | null
  dueTime?: string | null
  priority?: Priority
  isFlagged?: boolean
}
