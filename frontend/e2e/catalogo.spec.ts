import { test, expect } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'

test.describe('Catálogo', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
    await page.goto('/catalogo')
    await expect(page.getByRole('heading', { name: 'Catálogo' })).toBeVisible()
  })

  test('exibe campo de busca e botão Novo Produto', async ({ page }) => {
    await expect(page.getByPlaceholder('Buscar produto...')).toBeVisible()
    await expect(page.getByRole('button', { name: 'Novo Produto' })).toBeVisible()
  })

  test('abre dialog ao clicar em Novo Produto', async ({ page }) => {
    await page.getByRole('button', { name: 'Novo Produto' }).click()

    await expect(page.getByRole('dialog')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Novo Produto' })).toBeVisible()
    await expect(page.getByLabel('Nome')).toBeVisible()
    await expect(page.getByLabel('Valor Diária (R$)')).toBeVisible()
  })

  test('exibe erros de validação ao submeter form vazio', async ({ page }) => {
    await page.getByRole('button', { name: 'Novo Produto' }).click()
    await page.getByRole('button', { name: 'Criar' }).click()

    await expect(page.getByText(/nome deve ter ao menos/i)).toBeVisible()
  })

  test('fecha o dialog ao clicar em Cancelar', async ({ page }) => {
    await page.getByRole('button', { name: 'Novo Produto' }).click()
    await expect(page.getByRole('dialog')).toBeVisible()

    await page.getByRole('button', { name: 'Cancelar' }).click()
    await expect(page.getByRole('dialog')).not.toBeVisible()
  })

  test('campo de busca filtra os produtos exibidos', async ({ page }) => {
    // Aguarda os cards carregarem
    await page.waitForResponse((r) => r.url().includes('/v1/catalogo') && r.status() === 200)

    const search = page.getByPlaceholder('Buscar produto...')
    await search.fill('zzz_produto_inexistente_xyz')

    // Sem resultados — nenhum card de produto deve aparecer
    const cards = page.locator('[class*="CardContent"]')
    await expect(cards).toHaveCount(0)
  })
})
