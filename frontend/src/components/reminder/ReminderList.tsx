'use client'

import { useState } from 'react'
import { DndContext, DragEndEvent, PointerSensor, useSensor, useSensors } from '@dnd-kit/core'
import { SortableContext, arrayMove, verticalListSortingStrategy } from '@dnd-kit/sortable'
import { useApp } from '@/context/AppContext'
import ReminderItem from './ReminderItem'
import type { SmartFilter } from '@/types'

const SMART_LABELS: Record<SmartFilter, { label: string; color: string }> = {
  today:     { label: '오늘',    color: '#FF9500' },
  scheduled: { label: '예정',    color: '#007AFF' },
  all:       { label: '전체',    color: '#8E8E93' },
  flagged:   { label: '플래그됨', color: '#FF3B30' },
  completed: { label: '완료됨',  color: '#34C759' },
}

export default function ReminderListPanel() {
  const { lists, reminders, selectedId, selectedReminderId, createReminder, reorderReminders } = useApp()
  const [newTitle, setNewTitle] = useState('')
  const [inputVisible, setInputVisible] = useState(false)

  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 5 } }))

  const currentList = typeof selectedId === 'number'
    ? lists.find(l => l.id === selectedId)
    : null

  const smartInfo = typeof selectedId === 'string'
    ? SMART_LABELS[selectedId as SmartFilter]
    : null

  const headerColor = currentList?.color ?? smartInfo?.color ?? '#007AFF'
  const headerLabel = currentList?.name ?? smartInfo?.label ?? ''

  if (selectedId === null) {
    return (
      <div className="flex-1 flex items-center justify-center text-gray-400 text-sm">
        목록을 선택하세요
      </div>
    )
  }

  const handleAddReminder = async () => {
    const title = newTitle.trim()
    if (!title || typeof selectedId !== 'number') return
    await createReminder(selectedId, { title })
    setNewTitle('')
    setInputVisible(false)
  }

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event
    if (!over || active.id === over.id || typeof selectedId !== 'number') return

    const oldIndex = reminders.findIndex(r => r.id === active.id)
    const newIndex = reminders.findIndex(r => r.id === over.id)
    const newOrderIds = arrayMove(reminders, oldIndex, newIndex).map(r => r.id)

    try {
      await reorderReminders(selectedId, newOrderIds)
    } catch {
      // reorderReminders 내부에서 롤백 처리됨
    }
  }

  return (
    <div className="flex-1 flex flex-col bg-white overflow-hidden">
      {/* 헤더 */}
      <div className="px-6 pt-8 pb-4">
        <h1 className="text-3xl font-bold" style={{ color: headerColor }}>
          {headerLabel}
        </h1>
        <div className="text-sm text-gray-400 mt-0.5">
          {reminders.length}개의 리마인더
        </div>
      </div>

      {/* 리마인더 목록 */}
      <div className="flex-1 overflow-y-auto">
        <DndContext sensors={sensors} onDragEnd={handleDragEnd}>
          <SortableContext items={reminders.map(r => r.id)} strategy={verticalListSortingStrategy}>
            {reminders.map(reminder => (
              <ReminderItem
                key={reminder.id}
                reminder={reminder}
                list={currentList ?? lists.find(l => l.id === reminder.listId)}
                isSelected={selectedReminderId === reminder.id}
              />
            ))}
          </SortableContext>
        </DndContext>

        {/* 새 리마인더 입력 */}
        {inputVisible && typeof selectedId === 'number' && (
          <div className="flex items-center gap-3 px-4 py-2.5 border-b border-gray-100">
            <div className="w-5 h-5 rounded-full border-2 flex-shrink-0"
              style={{ borderColor: headerColor }} />
            <input
              autoFocus
              placeholder="새 리마인더"
              className="flex-1 text-sm outline-none"
              value={newTitle}
              onChange={e => setNewTitle(e.target.value)}
              onKeyDown={e => {
                if (e.key === 'Enter') handleAddReminder()
                if (e.key === 'Escape') { setNewTitle(''); setInputVisible(false) }
              }}
              onBlur={() => { if (!newTitle.trim()) setInputVisible(false) }}
            />
          </div>
        )}
      </div>

      {/* 추가 버튼 */}
      {typeof selectedId === 'number' && (
        <div className="px-4 py-3 border-t border-gray-100">
          <button
            className="flex items-center gap-1.5 text-blue-500 text-sm"
            onClick={() => setInputVisible(true)}
          >
            <span className="text-lg leading-none">+</span> 새 리마인더
          </button>
        </div>
      )}
    </div>
  )
}
