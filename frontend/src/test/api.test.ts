import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import * as api from '@/lib/api'

describe('api 클라이언트', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.useRealTimers()
  })

  it('10초 안에 응답 없으면 요청이 abort된다', async () => {
    vi.useFakeTimers()

    // fetch가 영원히 응답하지 않는 상황 시뮬레이션
    vi.mocked(fetch).mockImplementation((_url, init) => {
      return new Promise((_resolve, reject) => {
        const signal = (init as RequestInit)?.signal
        if (signal) {
          signal.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')))
        }
        // 응답 안 함 (hang)
      })
    })

    const promise = api.getLists()
    // 10초 경과
    vi.advanceTimersByTime(10000)

    await expect(promise).rejects.toThrow()
  })

  it('서버 에러 응답 시 에러 메시지를 포함한 Error를 던진다', async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify({ message: '서버 오류' }), { status: 500 })
    )

    await expect(api.getLists()).rejects.toThrow('서버 오류')
  })
})
