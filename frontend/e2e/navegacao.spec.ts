import { test, expect } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'

test.describe('Navegação — sidebar', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
  })

  const rotas = [
    { link: 'Catálogo', url: /catalogo/ },
    { link: 'Clientes', url: /clientes/ },
    { link: 'Locações', url: /locacoes/ },
    { link: 'Estoque', url: /estoque/ },
    { link: 'Pagamentos', url: /pagamentos/ },
    { link: 'Relatórios', url: /relatorios/ },
  ]

  for (const { link, url } of rotas) {
    test(`navega para ${link} via sidebar`, async ({ page }) => {
      await page.getByRole('link', { name: link }).click()
      await expect(page).toHaveURL(url)
    })
  }
})

test.describe('Página 404', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
  })

  test('exibe página not-found para rota inexistente', async ({ page }) => {
    await page.goto('/rota-que-nao-existe')
    await expect(page.getByText('Página não encontrada')).toBeVisible()
    await expect(page.getByText('404')).toBeVisible()
    await expect(page.getByRole('link', { name: /voltar ao painel/i })).toBeVisible()
  })

  test('botão "Voltar ao painel" na 404 navega para dashboard', async ({ page }) => {
    await page.goto('/rota-que-nao-existe')
    await page.getByRole('link', { name: /voltar ao painel/i }).click()
    await expect(page).toHaveURL(/dashboard/)
  })
})
