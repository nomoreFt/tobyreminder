'use client'

import { useEffect, useState } from 'react'
import { useApp } from '@/context/AppContext'
import type { Priority, Reminder } from '@/types'

const PRIORITIES: { value: Priority; label: string; color: string }[] = [
  { value: 'NONE',   label: '없음',  color: '#8E8E93' },
  { value: 'LOW',    label: '낮음',  color: '#007AFF' },
  { value: 'MEDIUM', label: '보통',  color: '#FF9500' },
  { value: 'HIGH',   label: '높음',  color: '#FF3B30' },
]

export default function DetailPanel() {
  const { reminders, selectedReminderId, updateReminder, selectReminder } = useApp()
  const reminder = reminders.find(r => r.id === selectedReminderId) ?? null

  const [title, setTitle] = useState('')
  const [notes, setNotes] = useState('')
  const [dueDate, setDueDate] = useState('')
  const [priority, setPriority] = useState<Priority>('NONE')
  const [isFlagged, setIsFlagged] = useState(false)

  useEffect(() => {
    if (reminder) {
      setTitle(reminder.title)
      setNotes(reminder.notes ?? '')
      setDueDate(reminder.dueDate ?? '')
      setPriority(reminder.priority)
      setIsFlagged(reminder.isFlagged)
    }
  }, [reminder?.id])

  if (!reminder) return null

  const save = (patch: Partial<Reminder>) => {
    updateReminder(reminder.id, {
      title:    patch.title    ?? title,
      notes:    patch.notes    ?? (notes || null),
      dueDate:  patch.dueDate  !== undefined ? patch.dueDate  : (dueDate || null),
      priority: patch.priority ?? priority,
      isFlagged: patch.isFlagged ?? isFlagged,
    })
  }

  return (
    <aside className="w-72 bg-[#F9F9F9] border-l border-gray-200 flex flex-col h-full">
      {/* 헤더 */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-gray-200">
        <span className="text-sm font-semibold text-gray-600">세부 정보</span>
        <button
          className="text-gray-400 hover:text-gray-600 text-sm"
          onClick={() => selectReminder(null)}
        >
          ✕
        </button>
      </div>

      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {/* 제목 */}
        <div>
          <label className="text-xs text-gray-400 font-medium uppercase tracking-wide">제목</label>
          <input
            className="w-full mt-1 text-sm bg-white border border-gray-200 rounded-lg px-3 py-2 outline-none focus:border-blue-400"
            value={title}
            onChange={e => setTitle(e.target.value)}
            onBlur={() => save({ title })}
          />
        </div>

        {/* 메모 */}
        <div>
          <label className="text-xs text-gray-400 font-medium uppercase tracking-wide">메모</label>
          <textarea
            className="w-full mt-1 text-sm bg-white border border-gray-200 rounded-lg px-3 py-2 outline-none focus:border-blue-400 resize-none"
            rows={3}
            placeholder="메모 추가..."
            value={notes}
            onChange={e => setNotes(e.target.value)}
            onBlur={() => save({ notes: notes || null })}
          />
        </div>

        {/* 마감일 */}
        <div>
          <label className="text-xs text-gray-400 font-medium uppercase tracking-wide">마감일</label>
          <input
            type="date"
            className="w-full mt-1 text-sm bg-white border border-gray-200 rounded-lg px-3 py-2 outline-none focus:border-blue-400"
            value={dueDate}
            onChange={e => {
              setDueDate(e.target.value)
              save({ dueDate: e.target.value || null })
            }}
          />
        </div>

        {/* 우선순위 */}
        <div>
          <label className="text-xs text-gray-400 font-medium uppercase tracking-wide">우선순위</label>
          <div className="grid grid-cols-4 gap-1 mt-1">
            {PRIORITIES.map(p => (
              <button
                key={p.value}
                className={`text-xs py-1.5 rounded-lg border transition-all ${
                  priority === p.value ? 'text-white font-medium' : 'bg-white text-gray-600 border-gray-200'
                }`}
                style={priority === p.value ? { backgroundColor: p.color, borderColor: p.color } : {}}
                onClick={() => { setPriority(p.value); save({ priority: p.value }) }}
              >
                {p.label}
              </button>
            ))}
          </div>
        </div>

        {/* 플래그 */}
        <div>
          <button
            className={`flex items-center gap-2 text-sm px-3 py-2 rounded-lg w-full transition-colors ${
              isFlagged ? 'bg-red-50 text-red-500' : 'bg-white border border-gray-200 text-gray-600'
            }`}
            onClick={() => { setIsFlagged(!isFlagged); save({ isFlagged: !isFlagged }) }}
          >
            <span>🚩</span>
            <span>{isFlagged ? '플래그 해제' : '플래그 추가'}</span>
          </button>
        </div>
      </div>
    </aside>
  )
}
