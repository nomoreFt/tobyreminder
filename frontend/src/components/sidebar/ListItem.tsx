'use client'

import { useState } from 'react'
import { useApp } from '@/context/AppContext'
import type { ReminderList } from '@/types'

interface ListItemProps {
  list: ReminderList
  isSelected: boolean
}

export default function ListItem({ list, isSelected }: ListItemProps) {
  const { selectList, updateList, deleteList } = useApp()
  const [editing, setEditing] = useState(false)
  const [editName, setEditName] = useState(list.name)
  const [hovered, setHovered] = useState(false)

  const commitEdit = async () => {
    const name = editName.trim()
    if (name && name !== list.name) {
      await updateList(list.id, { name, color: list.color, icon: list.icon })
    } else {
      setEditName(list.name)
    }
    setEditing(false)
  }

  return (
    <div
      className={`
        flex items-center gap-2 px-3 py-1.5 mx-1 rounded-lg cursor-pointer
        transition-colors group
        ${isSelected ? 'bg-blue-100' : 'hover:bg-gray-200'}
      `}
      onClick={() => !editing && selectList(list.id)}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      {/* 아이콘 */}
      <div
        className="w-6 h-6 rounded-md flex items-center justify-center flex-shrink-0 text-white text-xs"
        style={{ backgroundColor: list.color }}
      >
        ●
      </div>

      {/* 이름 (인라인 편집) */}
      {editing ? (
        <input
          autoFocus
          className="flex-1 bg-transparent outline-none text-sm"
          value={editName}
          onChange={e => setEditName(e.target.value)}
          onBlur={commitEdit}
          onKeyDown={e => {
            if (e.key === 'Enter') commitEdit()
            if (e.key === 'Escape') { setEditName(list.name); setEditing(false) }
          }}
          onClick={e => e.stopPropagation()}
        />
      ) : (
        <span
          className="flex-1 text-sm truncate"
          onDoubleClick={e => { e.stopPropagation(); setEditing(true) }}
        >
          {list.name}
        </span>
      )}

      {/* 미완료 카운트 / 삭제 버튼 */}
      {hovered && !editing ? (
        <button
          className="text-gray-400 hover:text-red-500 text-xs transition-colors"
          onClick={e => { e.stopPropagation(); deleteList(list.id) }}
        >
          ✕
        </button>
      ) : list.reminderCount > 0 ? (
        <span className="text-xs text-gray-400">{list.reminderCount}</span>
      ) : null}
    </div>
  )
}
