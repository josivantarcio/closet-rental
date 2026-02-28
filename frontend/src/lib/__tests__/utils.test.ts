import { describe, it, expect } from 'vitest'
import { cn } from '@/lib/utils'

describe('cn', () => {
  it('merges two class strings', () => {
    expect(cn('foo', 'bar')).toBe('foo bar')
  })

  it('tailwind-merge resolves conflicting utilities (last wins)', () => {
    expect(cn('p-4', 'p-2')).toBe('p-2')
  })

  it('ignores undefined values', () => {
    expect(cn(undefined, 'text-red')).toBe('text-red')
  })

  it('ignores falsy values', () => {
    expect(cn(false, null, 'bg-blue-500')).toBe('bg-blue-500')
  })

  it('returns empty string when no classes provided', () => {
    expect(cn()).toBe('')
  })

  it('handles conditional classes', () => {
    const active = true
    expect(cn('base', active && 'active')).toBe('base active')
  })
})
