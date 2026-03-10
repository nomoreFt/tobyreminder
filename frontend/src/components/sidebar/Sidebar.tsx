'use client'

import { useState } from 'react'
import { useApp } from '@/context/AppContext'
import SmartLists from './SmartLists'
import ListItem from './ListItem'
import ColorPicker from '@/components/ui/ColorPicker'

export default function Sidebar() {
  const { lists, selectedId, createList } = useApp()
  const [adding, setAdding] = useState(false)
  const [newName, setNewName] = useState('')
  const [newColor, setNewColor] = useState('#007AFF')

  const commitAdd = async () => {
    const name = newName.trim()
    if (name) await createList({ name, color: newColor })
    setNewName('')
    setNewColor('#007AFF')
    setAdding(false)
  }

  return (
    <aside className="w-64 bg-[#F2F2F7] flex flex-col h-full border-r border-gray-200 select-none">
      {/* 스마트 목록 */}
      <SmartLists />

      <div className="px-3 pt-2 pb-1">
        <span className="text-xs font-semibold text-gray-400 uppercase tracking-wide px-2">내 목록</span>
      </div>

      {/* 목록 */}
      <div className="flex-1 overflow-y-auto pb-2">
        {lists.map(list => (
          <ListItem
            key={list.id}
            list={list}
            isSelected={selectedId === list.id}
          />
        ))}

        {/* 새 목록 추가 폼 */}
        {adding && (
          <div className="mx-2 mt-1 p-2 bg-white rounded-xl shadow-sm space-y-2">
            <input
              autoFocus
              placeholder="목록 이름"
              className="w-full text-sm outline-none"
              value={newName}
              onChange={e => setNewName(e.target.value)}
              onKeyDown={e => {
                if (e.key === 'Enter') commitAdd()
                if (e.key === 'Escape') { setAdding(false); setNewName('') }
              }}
            />
            <ColorPicker value={newColor} onChange={setNewColor} />
            <div className="flex justify-end gap-2 pt-1">
              <button className="text-xs text-gray-400" onClick={() => { setAdding(false); setNewName('') }}>취소</button>
              <button className="text-xs text-blue-500 font-medium" onClick={commitAdd}>추가</button>
            </div>
          </div>
        )}
      </div>

      {/* 목록 추가 버튼 */}
      <div className="px-3 pb-4">
        <button
          className="flex items-center gap-1.5 text-blue-500 text-sm font-medium py-1"
          onClick={() => setAdding(true)}
        >
          <span className="text-lg leading-none">+</span> 목록 추가
        </button>
      </div>
    </aside>
  )
}
