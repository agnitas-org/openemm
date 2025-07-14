/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.awt.Dimension;
import java.util.Optional;

import com.agnitas.service.ImageDimensionService;
import com.agnitas.util.preview.impl.PuppeteerServiceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ImageDimensionServiceImpl implements ImageDimensionService {

    private static final Logger logger = LogManager.getLogger(ImageDimensionServiceImpl.class);

    @Override
    public Optional<Dimension> detectDimension(String url) {
        ResponseEntity<Dimension> response = new RestTemplate().getForEntity(
                "%s/dimension?imageUrl=%s".formatted(PuppeteerServiceManager.PUPPETEER_SERVICE_URL, url),
                Dimension.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return Optional.ofNullable(response.getBody());
        }

        logger.error("Dimension can't be determined for image with url <{}>", url);
        return Optional.empty();
    }
}
