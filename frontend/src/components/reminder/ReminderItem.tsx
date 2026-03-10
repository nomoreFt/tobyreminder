'use client'

import { useApp } from '@/context/AppContext'
import CircleCheckbox from '@/components/ui/CircleCheckbox'
import type { Reminder, ReminderList } from '@/types'

interface ReminderItemProps {
  reminder: Reminder
  list: ReminderList | undefined
  isSelected: boolean
}

const PRIORITY_ICON: Record<string, { icon: string; color: string }> = {
  LOW:    { icon: '!',   color: '#007AFF' },
  MEDIUM: { icon: '!!',  color: '#FF9500' },
  HIGH:   { icon: '!!!', color: '#FF3B30' },
}

export default function ReminderItem({ reminder, list, isSelected }: ReminderItemProps) {
  const { toggleComplete, deleteReminder, selectReminder } = useApp()
  const color = list?.color ?? '#007AFF'
  const priority = PRIORITY_ICON[reminder.priority]

  return (
    <div
      className={`
        flex items-start gap-3 px-4 py-2.5 cursor-pointer group transition-colors
        ${isSelected ? 'bg-blue-50' : 'hover:bg-gray-50'}
        border-b border-gray-100 last:border-0
      `}
      onClick={() => selectReminder(reminder.id)}
    >
      <div className="pt-0.5">
        <CircleCheckbox
          checked={reminder.isCompleted}
          color={color}
          onChange={() => toggleComplete(reminder.id)}
        />
      </div>

      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-1">
          <span className={`text-sm ${reminder.isCompleted ? 'line-through text-gray-400' : 'text-gray-900'}`}>
            {reminder.title}
          </span>
          {reminder.isFlagged && <span className="text-xs text-red-500">🚩</span>}
          {priority && (
            <span className="text-xs font-bold" style={{ color: priority.color }}>{priority.icon}</span>
          )}
        </div>
        {reminder.dueDate && (
          <div className="text-xs text-gray-400 mt-0.5">{reminder.dueDate}</div>
        )}
        {reminder.notes && (
          <div className="text-xs text-gray-400 truncate mt-0.5">{reminder.notes}</div>
        )}
      </div>

      <button
        className="opacity-0 group-hover:opacity-100 text-gray-300 hover:text-red-400 text-sm transition-all"
        onClick={e => { e.stopPropagation(); deleteReminder(reminder.id) }}
      >
        ✕
      </button>
    </div>
  )
}
