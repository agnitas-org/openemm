const { Cluster } = require('puppeteer-cluster');
const express = require('express');
const app = express();

const PORT = 3000;
const MAX_CONCURRENCY = 4; // 4 parallel tabs; ensures we never have 50 tabs open

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

let cluster;

(async () => {
  console.log(`Launching Puppeteer Cluster with ${MAX_CONCURRENCY} workers...`);

  cluster = await Cluster.launch({
    concurrency: Cluster.CONCURRENCY_CONTEXT, // Uses Incognito pages (fast and isolated)
    maxConcurrency: MAX_CONCURRENCY,
    // monitor: true, // prints simple stats to console. uncomment for debug purpose
    puppeteerOptions: {
      headless: "new",
      protocolTimeout: 360_000, // 6 minutes. Default 3 minutes
      args: minimal_args
    }
  });

  cluster.on('taskerror', (err, data) => { // Global Error Handler
    console.error(`Error processing task: ${err.message}`, data);
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

  ['SIGTERM', 'SIGINT'].forEach(signal => {
    process.on(signal, async () => {
      console.log(`Received ${signal}, closing cluster...`);
      await cluster.close();
      console.log('Cluster closed, exiting...');
      process.exit(0);
    });
  });

  console.log('Cluster is ready.');
})();

app.use(express.json()); // Middleware to parse JSON bodies

const screenshotTask = async ({ page, data }) => {
  const { url, path, timeout, requestedWidth } = data;
  let width = requestedWidth;

  await page.setDefaultNavigationTimeout(timeout || 30000);
  await page.goto(url, { waitUntil: 'load' });

  if (!width) {
    width = await page.evaluate(() => document.body.scrollWidth);
  }

  await page.setViewport({ width: +width, height: 1 });
  await page.screenshot({
    path,
    fullPage: true,
    type: 'jpeg',
    quality: 50,
    optimizeForSpeed: true
  });
  return { message: 'Screenshot created', path };
};

const pdfTask = async ({ page, data }) => {
  const { url, path, landscape, customCssPath, windowWaitStatus, timeout } = data;

  await page.setDefaultNavigationTimeout(timeout || 30000);
  await page.goto(url, { waitUntil: 'networkidle2' });

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

  return { message: 'Pdf created', path };
};

const dimensionTask = async ({ page, data }) => {
  const { imageUrl } = data;

  await page.setContent(`
      <!DOCTYPE html>
      <html>
          <body>
              <img id="imageToDimensionCheck" src="${imageUrl}" style="display: none;" />
          </body>
      </html>
  `);

  await page.waitForSelector('#imageToDimensionCheck');

  return await page.evaluate(() => {
    const img = document.querySelector(`img`);
    return {
      width: img.naturalWidth,
      height: img.naturalHeight
    };
  });
};

app.post('/screenshot', async (req, res) => {
  const { url, path, timeout } = req.body;
  const requestedWidth = req.body.width;

  try {
    const result = await cluster.execute(
      { url, path, timeout, requestedWidth },
      screenshotTask
    );
    res.status(200).json(result);
  } catch (error) {
    console.error(`Screenshot failed for ${url}:`, error);
    res.status(500).json({ message: 'Screenshot failed', error: error.message });
  }
});

app.post('/pdf', async (req, res) => {
  try {
    const result = await cluster.execute(req.body, pdfTask);
    res.status(200).json(result);
  } catch (error) {
    console.error(`PDF failed for ${req.body.url}:`, error);
    res.status(500).json({ message: 'Pdf failed', error: error.message });
  }
});

app.get('/dimension', async (req, res) => {
  const { imageUrl } = req.query;

  if (!imageUrl) {
    return res.status(400).json({ message: 'Image URL is required' });
  }

  try {
    const result = await cluster.execute({ imageUrl }, dimensionTask);
    res.status(200).json(result);
  } catch (error) {
    console.error(`Dimension detection failed ${imageUrl}:`, error);
    res.status(500).json({ message: 'Dimension detection failed', error: error.message });
  }
});
