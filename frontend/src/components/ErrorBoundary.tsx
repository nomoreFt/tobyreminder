'use client'

import React from 'react'

interface State { hasError: boolean; message: string }

export class ErrorBoundary extends React.Component<
  { children: React.ReactNode; fallback?: React.ReactNode },
  State
> {
  state: State = { hasError: false, message: '' }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, message: error.message }
  }

  render() {
    if (this.state.hasError) {
      return this.props.fallback ?? (
        <div className="flex items-center justify-center h-screen text-red-500 text-sm">
          오류가 발생했습니다: {this.state.message}
        </div>
      )
    }
    return this.props.children
  }
}
