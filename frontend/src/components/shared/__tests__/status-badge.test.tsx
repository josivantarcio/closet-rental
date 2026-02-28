import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import {
  LocacaoBadge,
  PagamentoBadge,
  DisponibilidadeBadge,
} from '@/components/shared/status-badge'

describe('LocacaoBadge', () => {
  it('renders "Reservado" for RESERVADO status', () => {
    render(<LocacaoBadge status="RESERVADO" />)
    expect(screen.getByText('Reservado')).toBeInTheDocument()
  })

  it('renders "Retirado" for RETIRADO status', () => {
    render(<LocacaoBadge status="RETIRADO" />)
    expect(screen.getByText('Retirado')).toBeInTheDocument()
  })

  it('renders "Devolvido" for DEVOLVIDO status', () => {
    render(<LocacaoBadge status="DEVOLVIDO" />)
    expect(screen.getByText('Devolvido')).toBeInTheDocument()
  })

  it('renders "Cancelado" for CANCELADO status', () => {
    render(<LocacaoBadge status="CANCELADO" />)
    expect(screen.getByText('Cancelado')).toBeInTheDocument()
  })
})

describe('PagamentoBadge', () => {
  it('renders "Aprovado" for APROVADO status', () => {
    render(<PagamentoBadge status="APROVADO" />)
    expect(screen.getByText('Aprovado')).toBeInTheDocument()
  })

  it('renders "Pendente" for PENDENTE status', () => {
    render(<PagamentoBadge status="PENDENTE" />)
    expect(screen.getByText('Pendente')).toBeInTheDocument()
  })

  it('renders "Falha" for FALHA status', () => {
    render(<PagamentoBadge status="FALHA" />)
    expect(screen.getByText('Falha')).toBeInTheDocument()
  })

  it('renders "Estornado" for ESTORNADO status', () => {
    render(<PagamentoBadge status="ESTORNADO" />)
    expect(screen.getByText('Estornado')).toBeInTheDocument()
  })
})

describe('DisponibilidadeBadge', () => {
  it('renders "Disponível" when disponivel is true', () => {
    render(<DisponibilidadeBadge disponivel={true} />)
    expect(screen.getByText('Disponível')).toBeInTheDocument()
  })

  it('renders "Indisponível" when disponivel is false', () => {
    render(<DisponibilidadeBadge disponivel={false} />)
    expect(screen.getByText('Indisponível')).toBeInTheDocument()
  })
})
