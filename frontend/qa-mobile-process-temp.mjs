export default async function run(page) {
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('http://localhost:5173/')
  await page.waitForSelector('.process-step')
  await page.screenshot({ path: 'C:/Users/Admin/Documents/Projects/sanele-portfolio-platform/frontend/mobile-process-check.png', fullPage: true })
  return await page.evaluate(() => ({
    steps: document.querySelectorAll('.process-step').length,
    columns: getComputedStyle(document.querySelector('.process-grid')).gridTemplateColumns,
    connectorVisible: [...document.querySelectorAll('.process-connector')].some((node) => getComputedStyle(node).display !== 'none'),
    ctaColumns: getComputedStyle(document.querySelector('.home-cta')).gridTemplateColumns,
    seamDisplay: getComputedStyle(document.querySelector('.home-cta .contact-image'), '::before').display,
    viewport: innerWidth,
    scrollWidth: document.documentElement.scrollWidth,
  }))
}
