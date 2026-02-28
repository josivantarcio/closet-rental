import { test, expect } from '@playwright/test'

test.describe('Autenticação', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
  })

  test('exibe a página de login corretamente', async ({ page }) => {
    await expect(page.getByRole('heading', { name: 'Atelier' })).toBeVisible()
    await expect(page.getByLabel('Usuário')).toBeVisible()
    await expect(page.getByLabel('Senha')).toBeVisible()
    await expect(page.getByRole('button', { name: 'Entrar' })).toBeVisible()
  })

  test('exibe hints das contas de teste', async ({ page }) => {
    await expect(page.getByText('Contas de teste')).toBeVisible()
    await expect(page.getByText('admin')).toBeVisible()
  })

  test('login com admin redireciona para dashboard', async ({ page }) => {
    await page.getByLabel('Usuário').fill('admin')
    await page.getByLabel('Senha').fill('admin123')
    await page.getByRole('button', { name: 'Entrar' }).click()

    await page.waitForURL('**/dashboard')
    await expect(page).toHaveURL(/dashboard/)
    await expect(page.getByText('Dashboard')).toBeVisible()
  })

  test('login com atendente redireciona para dashboard', async ({ page }) => {
    await page.getByLabel('Usuário').fill('atendente')
    await page.getByLabel('Senha').fill('atendente123')
    await page.getByRole('button', { name: 'Entrar' }).click()

    await page.waitForURL('**/dashboard')
    await expect(page).toHaveURL(/dashboard/)
  })

  test('credenciais inválidas exibem mensagem de erro', async ({ page }) => {
    await page.getByLabel('Usuário').fill('usuario_invalido')
    await page.getByLabel('Senha').fill('senha_errada')
    await page.getByRole('button', { name: 'Entrar' }).click()

    await expect(page.getByText(/credenciais inválidas/i)).toBeVisible()
  })

  test('campos vazios exibem erros de validação', async ({ page }) => {
    await page.getByRole('button', { name: 'Entrar' }).click()

    await expect(page.getByText(/usuário obrigatório/i)).toBeVisible()
    await expect(page.getByText(/senha obrigatória/i)).toBeVisible()
  })

  test('rota protegida redireciona para login quando não autenticado', async ({ page }) => {
    await page.goto('/dashboard')
    await page.waitForURL('**/login')
    await expect(page).toHaveURL(/login/)
  })
})
