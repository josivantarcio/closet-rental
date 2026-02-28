import type { Page } from '@playwright/test'

export async function loginAs(page: Page, username: string, password: string) {
  await page.goto('/login')
  await page.getByLabel('Usuário').fill(username)
  await page.getByLabel('Senha').fill(password)
  await page.getByRole('button', { name: 'Entrar' }).click()
  // Aguarda redirecionamento para dashboard
  await page.waitForURL('**/dashboard')
}

export async function loginAsAdmin(page: Page) {
  return loginAs(page, 'admin', 'admin123')
}

export async function loginAsAtendente(page: Page) {
  return loginAs(page, 'atendente', 'atendente123')
}
