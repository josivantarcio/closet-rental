import { describe, it, expect } from 'vitest'
import {
  formatCurrency,
  formatDate,
  formatDateTime,
  formatDocumento,
  metodoPagamentoLabel,
  categoriaLabel,
} from '@/lib/format'

describe('formatCurrency', () => {
  it('formats value as BRL currency', () => {
    expect(formatCurrency(1500)).toBe('R$\u00a01.500,00')
  })

  it('formats zero correctly', () => {
    expect(formatCurrency(0)).toBe('R$\u00a00,00')
  })

  it('formats decimal value correctly', () => {
    expect(formatCurrency(99.9)).toBe('R$\u00a099,90')
  })
})

describe('formatDate', () => {
  it('formats ISO date to dd/MM/yyyy', () => {
    expect(formatDate('2024-03-15')).toBe('15/03/2024')
  })

  it('formats another date correctly', () => {
    expect(formatDate('2024-01-01')).toBe('01/01/2024')
  })
})

describe('formatDateTime', () => {
  it('formats ISO datetime to dd/MM/yyyy HH:mm', () => {
    expect(formatDateTime('2024-03-15T14:30:00')).toBe('15/03/2024 14:30')
  })

  it('formats midnight correctly', () => {
    expect(formatDateTime('2024-03-15T00:00:00')).toBe('15/03/2024 00:00')
  })
})

describe('formatDocumento', () => {
  it('formats CPF (11 digits)', () => {
    expect(formatDocumento('12345678901')).toBe('123.456.789-01')
  })

  it('formats CNPJ (14 digits)', () => {
    expect(formatDocumento('12345678000195')).toBe('12.345.678/0001-95')
  })

  it('strips non-digit characters before formatting', () => {
    expect(formatDocumento('123.456.789-01')).toBe('123.456.789-01')
  })

  it('returns "—" for null', () => {
    expect(formatDocumento(null)).toBe('—')
  })

  it('returns "—" for undefined', () => {
    expect(formatDocumento(undefined)).toBe('—')
  })

  it('returns "—" for empty string', () => {
    expect(formatDocumento('')).toBe('—')
  })
})

describe('metodoPagamentoLabel', () => {
  it('returns PIX label', () => {
    expect(metodoPagamentoLabel['PIX']).toBe('PIX')
  })

  it('returns Cartão de Crédito label', () => {
    expect(metodoPagamentoLabel['CARTAO_CREDITO']).toBe('Cartão de Crédito')
  })

  it('returns Cartão de Débito label', () => {
    expect(metodoPagamentoLabel['CARTAO_DEBITO']).toBe('Cartão de Débito')
  })

  it('returns Dinheiro label', () => {
    expect(metodoPagamentoLabel['DINHEIRO']).toBe('Dinheiro')
  })
})

describe('categoriaLabel', () => {
  it('returns Vestido label', () => {
    expect(categoriaLabel['VESTIDO']).toBe('Vestido')
  })

  it('returns Terno label', () => {
    expect(categoriaLabel['TERNO']).toBe('Terno')
  })

  it('returns Acessório label', () => {
    expect(categoriaLabel['ACESSORIO']).toBe('Acessório')
  })

  it('returns Outro label', () => {
    expect(categoriaLabel['OUTRO']).toBe('Outro')
  })
})
