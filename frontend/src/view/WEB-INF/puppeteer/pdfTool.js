'use strict';

/*
Usage: node pdfTool.js [PATH] [LANDSCAPE] [URL] [CUSTOM_STYLES_PATH] [WINDOW_WAIT_STATUS]
Example: node pdfTool.js ./example.pdf false https://example.com ./custom-styles.css
*/

const puppeteer = require('puppeteer');

const TOP_MARGIN = 88;    // px
const BOTTOM_MARGIN = 45; // px
const LEFT_MARGIN = 32;   // px
const RIGHT_MARGIN = 32;  // px

const sleep = ms => new Promise(res => setTimeout(res, ms));

(async () => {
  const args = process.argv.slice(2);
  const path = args[0];
  const landscape = args[1];
  const url = args[2];
  const customCssPath = args[3];
  const windowWaitStatus = args[4];

  const browser = await puppeteer.launch();
  const page = await browser.newPage();
  await page.goto(url, {waitUntil: 'networkidle2', timeout: 60000})
  await sleep(500); // wait for js render
  await page.addStyleTag({content: "body{-webkit-print-color-adjust: exact;}"}) // force rendering of the original colors
  if (customCssPath) {
    await page.addStyleTag({path: customCssPath});
  }

  if (windowWaitStatus) {
    await page.waitForFunction(
      (expectedStatus) => window.status === expectedStatus,
      {},
      windowWaitStatus
    );
  }

  const contentWidth = await page.evaluate(() => {
    return document.documentElement.clientWidth;
  });

  const scaleFactor = (page.viewport().width - (LEFT_MARGIN + RIGHT_MARGIN)) / contentWidth;

  await page.pdf({
    path: path,
    format: 'A4',
    landscape: landscape === 'true',
    margin: { top: TOP_MARGIN, bottom: BOTTOM_MARGIN, left: LEFT_MARGIN, right: RIGHT_MARGIN},
    printBackground: true,
    scale: scaleFactor
  })
  await browser.close()
})();
