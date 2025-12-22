/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import com.agnitas.emm.core.components.service.MailingBlockSizeService;
import com.agnitas.emm.core.mailing.enums.BlocksizeSteppingOption;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("MailingBlockSizeService")
public class MailingBlockSizeServiceImpl implements MailingBlockSizeService {

    private static final Logger logger = LogManager.getLogger(MailingBlockSizeServiceImpl.class);

    private final ConfigService configService;

    @Autowired
    public MailingBlockSizeServiceImpl(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public Tuple<Integer, Integer> calculateBlocksizeStepping(int companyId, int stepping, int blocksize) {
        try {
            int maxBlocksize = getMaxBlocksize(companyId, blocksize);
            if (maxBlocksize != BlocksizeSteppingOption.UNLIMITED.getMailsPerHour()) {
                return AgnUtils.makeBlocksizeAndSteppingFromBlocksize(maxBlocksize, DEFAULT_STEPPING);
            }

            if (stepping == DEFAULT_STEPPING) {
                return AgnUtils.makeBlocksizeAndSteppingFromBlocksize(blocksize, stepping);
            }

            return new Tuple<>(blocksize, stepping);
        } catch (Exception e) {
            logger.error("Error occurred when calculate block size and stepping!", e);
        }

        return new Tuple<>(0, 0);
    }

    @Override
    public int calculateBlocksize(int stepping, int blockSize) {
        if (stepping == 15) {
            if (blockSize == 250) {
                return 1000;
            }

            if (blockSize == 1250) {
                return 5000;
            }

            if (blockSize == 2500) {
                return 10000;
            }
        }

        if (stepping == 5) {
            if (blockSize == 2083) {
                return 25000;
            }

            if (blockSize == 4166) {
                return 50000;
            }
        }

        if (stepping == 1) {
            if (blockSize == 4166) {
                return 250000;
            }

            if (blockSize == 1666) {
                return 100000;
            }

            if (blockSize == 8333) {
                return 500000;
            }
        }

        return blockSize;
    }

    private int getMaxBlocksize(int companyID, int blocksize) {
        if (!configService.getBooleanValue(ConfigValue.ForceSteppingBlocksize, companyID)) {
            return BlocksizeSteppingOption.UNLIMITED.getMailsPerHour();
        }

        int maxBlocksize = configService.getIntegerValue(ConfigValue.DefaultBlocksizeValue, companyID);
        return Math.min(blocksize, maxBlocksize);
    }
}
