import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import MockAdapter from 'axios-mock-adapter'
import { api } from '@/lib/api'
import { authService } from '@/lib/services/auth.service'

const mock = new MockAdapter(api)

beforeEach(() => mock.reset())
afterEach(() => mock.reset())

describe('authService.login', () => {
  it('posts to /v1/auth/login and returns LoginResponse', async () => {
    const response = {
      token: 'jwt-token-123',
      username: 'admin',
      roles: ['ROLE_ADMIN'],
    }
    mock.onPost('/v1/auth/login').reply(200, response)

    const result = await authService.login({ username: 'admin', password: 'admin123' })

    expect(result).toEqual(response)
    expect(mock.history.post).toHaveLength(1)
    expect(mock.history.post[0].url).toBe('/v1/auth/login')
  })

  it('sends correct credentials in request body', async () => {
    mock.onPost('/v1/auth/login').reply(200, {
      token: 'tok',
      username: 'atendente',
      roles: ['ROLE_ATENDENTE'],
    })

    await authService.login({ username: 'atendente', password: 'atendente123' })

    const body = JSON.parse(mock.history.post[0].data)
    expect(body.username).toBe('atendente')
    expect(body.password).toBe('atendente123')
  })

  it('throws error on 401', async () => {
    // Disable redirect interceptor for this test by using a URL with no window redirect
    mock.onPost('/v1/auth/login').reply(401, { message: 'Credenciais inválidas' })

    await expect(
      authService.login({ username: 'wrong', password: 'wrong' })
    ).rejects.toThrow()
  })
})
