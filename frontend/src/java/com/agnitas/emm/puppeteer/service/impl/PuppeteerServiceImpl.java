/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.puppeteer.service.impl;

import com.agnitas.emm.core.serverstatus.PuppeteerStatus;
import com.agnitas.emm.puppeteer.PuppeteerServiceManager;
import com.agnitas.emm.puppeteer.service.PuppeteerService;
import org.springframework.stereotype.Service;

@Service("puppeteerService")
public class PuppeteerServiceImpl implements PuppeteerService {

    private static final String SCREENSHOT_URL = PuppeteerServiceManager.PUPPETEER_SERVICE_URL + "/screenshot";
    private static final String PDF_URL = PuppeteerServiceManager.PUPPETEER_SERVICE_URL + "/pdf";

    private final PuppeteerServiceManager puppeteerServiceManager;

    public PuppeteerServiceImpl(PuppeteerServiceManager puppeteerServiceManager) {
        this.puppeteerServiceManager = puppeteerServiceManager;
    }

    @Override
    public String getScreenshotUrl() {
        return SCREENSHOT_URL;
    }

    @Override
    public String getPdfUrl() {
        return PDF_URL;
    }

    @Override
    public String getDimensionUrl(String imageUrl) {
        return "%s/dimension?imageUrl=%s".formatted(PuppeteerServiceManager.PUPPETEER_SERVICE_URL, imageUrl);
    }

    @Override
    public boolean isServiceRunning() {
        return puppeteerServiceManager.isRunning();
    }

    @Override
    public void startService() {
        puppeteerServiceManager.start();
    }

    @Override
    public PuppeteerStatus getStatus() {
        if (!puppeteerServiceManager.isInstalled()) {
            return PuppeteerStatus.MISSING;
        }

        return isServiceRunning() ? PuppeteerStatus.RUNNING : PuppeteerStatus.STOPPED;
    }

}
