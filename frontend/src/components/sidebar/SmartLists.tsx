'use client'

import { useApp } from '@/context/AppContext'
import type { SmartFilter } from '@/types'

const SMART_ITEMS: { filter: SmartFilter; label: string; icon: string; color: string }[] = [
  { filter: 'today',     label: '오늘',    icon: '☀️', color: '#FF9500' },
  { filter: 'scheduled', label: '예정',    icon: '📅', color: '#007AFF' },
  { filter: 'all',       label: '전체',    icon: '📋', color: '#8E8E93' },
  { filter: 'flagged',   label: '플래그됨', icon: '🚩', color: '#FF3B30' },
]

export default function SmartLists() {
  const { lists, selectedId, selectList, smartCounts } = useApp()

  const counts: Record<SmartFilter, number> = {
    today:     smartCounts.today,
    scheduled: smartCounts.scheduled,
    all:       lists.reduce((sum, l) => sum + l.reminderCount, 0),
    flagged:   smartCounts.flagged,
    completed: 0,
  }

  return (
    <div className="grid grid-cols-2 gap-2 px-3 py-2">
      {SMART_ITEMS.map(({ filter, label, icon, color }) => (
        <button
          key={filter}
          onClick={() => selectList(filter)}
          className={`
            flex flex-col p-3 rounded-xl text-left transition-all
            ${selectedId === filter ? 'brightness-90' : 'hover:brightness-95'}
          `}
          style={{ backgroundColor: `${color}22` }}
        >
          <span className="text-2xl mb-1">{icon}</span>
          <span className="text-xs text-gray-500 font-medium">{label}</span>
          {counts[filter] > 0 && (
            <span className="text-lg font-bold mt-0.5" style={{ color }}>
              {counts[filter]}
            </span>
          )}
        </button>
      ))}
    </div>
  )
}
