'use strict';

/*
Usage: node imageTool.js [PATH] [URL] [VIEWPORT_WIDTH]
Example: node imageTool.js ./example.png https://example.com 1366
*/

const puppeteer = require('puppeteer');

const args = process.argv.slice(2);
const path = args[0];
const url  = args[1];
let width  = args[2]; // px

(async () => {
  const browser = await puppeteer.launch();
  const page = await browser.newPage();
  if (width) {
    await page.setViewport({width: +width, height: 1});
  }
  await page.goto(url, { timeout: 60000 });
  await page.screenshot({path: path, fullPage: true});
  await browser.close();
})();
