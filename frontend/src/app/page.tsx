'use client'

import { useEffect } from 'react'
import { useApp } from '@/context/AppContext'
import Sidebar from '@/components/sidebar/Sidebar'
import ReminderListPanel from '@/components/reminder/ReminderList'
import DetailPanel from '@/components/reminder/DetailPanel'

export default function Home() {
  const { fetchLists, selectedReminderId } = useApp()

  useEffect(() => {
    fetchLists()
  }, [fetchLists])

  return (
    <div className="flex h-screen overflow-hidden bg-[#F2F2F7]">
      <Sidebar />
      <ReminderListPanel />
      {selectedReminderId !== null && <DetailPanel />}
    </div>
  )
}
