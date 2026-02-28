import { test, expect } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
  })

  test('exibe os 4 cards de KPI', async ({ page }) => {
    await expect(page.getByText('Peças em Uso')).toBeVisible()
    await expect(page.getByText('Atrasos Pendentes')).toBeVisible()
    await expect(page.getByText(/Receita/)).toBeVisible()
    await expect(page.getByText('Total de Peças')).toBeVisible()
  })

  test('exibe a seção de locações ativas', async ({ page }) => {
    // Título do card de peças em uso
    await expect(page.getByRole('heading', { name: 'Peças em Uso' })).toBeVisible()
  })

  test('exibe a seção de atrasos pendentes', async ({ page }) => {
    await expect(page.getByRole('heading', { name: 'Atrasos Pendentes' })).toBeVisible()
  })

  test('link "Ver todas" navega para locações', async ({ page }) => {
    await page.getByRole('link', { name: /ver todas/i }).first().click()
    await expect(page).toHaveURL(/locacoes/)
  })

  test('link "Ver relatório" navega para relatórios', async ({ page }) => {
    await page.getByRole('link', { name: /ver relatório/i }).click()
    await expect(page).toHaveURL(/relatorios/)
  })
})
