import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import MockAdapter from 'axios-mock-adapter'
import { api } from '@/lib/api'
import { catalogoService } from '@/lib/services/catalogo.service'
import type { ProdutoResponse, ProdutoRequest } from '@/types'

const mock = new MockAdapter(api)

beforeEach(() => mock.reset())
afterEach(() => mock.reset())

const produto: ProdutoResponse = {
  id: 1,
  nome: 'Vestido de Festa',
  categoria: 'VESTIDO',
  valorDiaria: 150,
  disponivel: true,
  createdBy: 'admin',
}

const produtoRequest: ProdutoRequest = {
  nome: 'Vestido de Festa',
  categoria: 'VESTIDO',
  valorDiaria: 150,
}

const pageResponse = {
  content: [produto],
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 12,
}

describe('catalogoService.listar', () => {
  it('sends GET /v1/catalogo with default pagination', async () => {
    mock.onGet('/v1/catalogo').reply(200, pageResponse)

    const result = await catalogoService.listar()

    expect(result).toEqual(pageResponse)
    expect(mock.history.get[0].url).toBe('/v1/catalogo')
    expect(mock.history.get[0].params).toEqual({ page: 0, size: 12 })
  })

  it('sends custom page and size', async () => {
    mock.onGet('/v1/catalogo').reply(200, pageResponse)

    await catalogoService.listar(2, 24)

    expect(mock.history.get[0].params).toEqual({ page: 2, size: 24 })
  })
})

describe('catalogoService.buscarPorId', () => {
  it('sends GET /v1/catalogo/:id and returns product', async () => {
    mock.onGet('/v1/catalogo/1').reply(200, produto)

    const result = await catalogoService.buscarPorId(1)

    expect(result).toEqual(produto)
    expect(mock.history.get[0].url).toBe('/v1/catalogo/1')
  })
})

describe('catalogoService.criar', () => {
  it('sends POST /v1/catalogo and returns created product', async () => {
    mock.onPost('/v1/catalogo').reply(201, produto)

    const result = await catalogoService.criar(produtoRequest)

    expect(result).toEqual(produto)
    expect(mock.history.post[0].url).toBe('/v1/catalogo')
    const body = JSON.parse(mock.history.post[0].data)
    expect(body.nome).toBe('Vestido de Festa')
  })
})

describe('catalogoService.atualizar', () => {
  it('sends PUT /v1/catalogo/:id and returns updated product', async () => {
    const updated = { ...produto, nome: 'Vestido Atualizado' }
    mock.onPut('/v1/catalogo/1').reply(200, updated)

    const result = await catalogoService.atualizar(1, { ...produtoRequest, nome: 'Vestido Atualizado' })

    expect(result.nome).toBe('Vestido Atualizado')
    expect(mock.history.put[0].url).toBe('/v1/catalogo/1')
  })
})

describe('catalogoService.deletar', () => {
  it('sends DELETE /v1/catalogo/:id', async () => {
    mock.onDelete('/v1/catalogo/1').reply(204)

    await catalogoService.deletar(1)

    expect(mock.history.delete[0].url).toBe('/v1/catalogo/1')
  })
})
