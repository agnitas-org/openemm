const puppeteer = require('puppeteer');
const express = require('express');
const app = express();

const PORT = 3000;
const TOP_MARGIN = 88;    // px
const BOTTOM_MARGIN = 45; // px
const LEFT_MARGIN = 32;   // px
const RIGHT_MARGIN = 32;  // px
const minimal_args = [
  '--autoplay-policy=user-gesture-required',
  '--disable-background-networking',
  '--disable-background-timer-throttling',
  '--disable-backgrounding-occluded-windows',
  '--disable-breakpad',
  '--disable-client-side-phishing-detection',
  '--disable-component-update',
  '--disable-default-apps',
  '--disable-dev-shm-usage',
  '--disable-domain-reliability',
  '--disable-extensions',
  '--disable-features=AudioServiceOutOfProcess',
  '--disable-hang-monitor',
  '--disable-ipc-flooding-protection',
  '--disable-notifications',
  '--disable-offer-store-unmasked-wallet-cards',
  '--disable-popup-blocking',
  '--disable-print-preview',
  '--disable-prompt-on-repost',
  '--disable-renderer-backgrounding',
  '--disable-speech-api',
  '--disable-sync',
  '--hide-scrollbars',
  '--ignore-gpu-blacklist',
  '--metrics-recording-only',
  '--mute-audio',
  '--no-default-browser-check',
  '--no-first-run',
  '--no-pings',
  '--password-store=basic',
  '--use-gl=swiftshader',
  '--use-mock-keychain',
];
const sleep = ms => new Promise(res => setTimeout(res, ms));

let browser;

(async () => {
  await launchBrowser();

  ['SIGTERM', 'SIGINT'].forEach(signal => {
    process.on(signal, async () => {
      console.log(`Received ${signal}, closing browser...`);
      if (browser) {
        await browser.close();
      }
      console.log('Browser closed, exiting...');
      process.exit(0);
    });
  });
})();

async function launchBrowser() {
  if (browser && browser.isConnected()) {
    console.log('Browser already running.');
    return;
  }
  console.log('Launching a new browser...');
  browser = await puppeteer.launch({ headless: true, args: minimal_args });
}

async function checkBrowser() {
  if (!browser || !browser.isConnected()) {
    console.log('Browser instance is not connected, relaunching...');
    await launchBrowser();
  }
}

app.use(express.json()); // Middleware to parse JSON bodies

app.post('/screenshot', async (req, res) => {
  const { url, path, timeout } = req.body;
  let width = req.body.width;

  await checkBrowser();
  const page = await browser.newPage();

  try {
    await page.goto(url, { timeout: timeout });

    if (!width) {
      width = await page.evaluate(() => document.body.scrollWidth);
    }

    await page.setViewport({ width: +width, height: 1 });
    await page.screenshot({ path, fullPage: true, type: 'jpeg', quality: 50, optimizeForSpeed: true });

    res.status(200).json({ message: 'Screenshot created', path });
  } catch (error) {
    console.error('Screenshot failed:', error);
    res.status(500).json({ message: 'Screenshot failed', error: error.message, stack: error.stack });
  } finally {
    await page.close();
  }
});

app.post('/pdf', async (req, res) => {
  const { url, path, landscape, customCssPath, windowWaitStatus, timeout } = req.body;

  await checkBrowser();
  const page = await browser.newPage();

  try {
    await page.goto(url, {waitUntil: 'networkidle2', timeout: timeout})

    if (windowWaitStatus) {
      await page.waitForFunction((expectedStatus) => window.waitStatus === expectedStatus, {}, windowWaitStatus);
    }

    await sleep(500); // wait for js render
    await page.addStyleTag({content: "body{-webkit-print-color-adjust: exact;}"}) // force rendering of the original colors

    if (customCssPath) {
      await page.addStyleTag({path: customCssPath});
    }

    const contentWidth = await page.evaluate(() => document.documentElement.clientWidth);
    const scaleFactor = Math.min(1.5, Math.max(0.1, (page.viewport().width - (LEFT_MARGIN + RIGHT_MARGIN)) / contentWidth)); // valid range [0.1 - 2], but at values greater than 1.5 the image may become cropped

    await page.pdf({
      path: path,
      format: 'A4',
      landscape,
      margin: { top: TOP_MARGIN, bottom: BOTTOM_MARGIN, left: LEFT_MARGIN, right: RIGHT_MARGIN},
      printBackground: true,
      scale: scaleFactor
    })

    res.status(200).json({ message: 'Pdf created', path });
  } catch (error) {
    console.error('Pdf failed:', error);
    res.status(500).json({message: 'Pdf failed', error: error.message, stack: error.stack});
  } finally {
    await page.close();
  }
});

app.get('/dimension', async (req, res) => {
  const { imageUrl } = req.query;

  if (!imageUrl) {
    return res.status(400).json({ message: 'Image URL is required' });
  }

  await checkBrowser();
  const page = await browser.newPage();

  try {
    await page.setContent(`
        <!DOCTYPE html>
        <html>
            <body>
                <img id="imageToDimensionCheck" src="${imageUrl}" style="display: none;" />
            </body>
        </html>
    `);

    await page.waitForSelector('#imageToDimensionCheck');

    const dimensions = await page.evaluate(() => {
      const img = document.querySelector(`img`);
      return {
        width: img.naturalWidth,
        height: img.naturalHeight
      };
    });

    res.status(200).json(dimensions);
  } catch (error) {
    console.error('Dimension detection failed:', error);
    res.status(500).json({ message: 'Dimension detection failed', error: error.message, stack: error.stack });
  } finally {
    await page.close();
  }
});

const server = app.listen(PORT, '127.0.0.1', () => {
  console.log(`Puppeteer service is running on http://localhost:${PORT}`);
});

server.on('error', (err) => {
  if (err.code === 'EADDRINUSE') {
    console.error(`Port ${PORT} in use. The Puppeteer service may already be running.`);
    process.exit(1);
  }
});
