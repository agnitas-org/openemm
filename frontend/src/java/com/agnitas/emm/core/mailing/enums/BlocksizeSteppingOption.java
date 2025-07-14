/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.enums;

import java.util.Optional;
import java.util.stream.Stream;

public enum BlocksizeSteppingOption {

    UNLIMITED(0, 0, 0),
    FIVE_HUNDRED_THOUSAND(500_000, 8333, 1),
    TWO_HUNDRED_FIFTY_THOUSAND(250_000, 4166, 1),
    ONE_HUNDRED_THOUSAND(100_000, 1666, 1),
    FIFTY_THOUSAND(50_000, 833, 1),
    TWENTY_FIVE_THOUSAND(25_000, 416, 1),
    TEN_THOUSAND(10_000, 500, 3),
    FIVE_THOUSAND(5_000, 1250, 15),
    ONE_THOUSAND(1_000, 250, 15);

    private final int mailsPerHour;
    private final int blockSize;
    private final int stepping;

    BlocksizeSteppingOption(int mailsPerHour, int blockSize, int stepping) {
        this.mailsPerHour = mailsPerHour;
        this.blockSize = blockSize;
        this.stepping = stepping;
    }

    public int getMailsPerHour() {
        return mailsPerHour;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getStepping() {
        return stepping;
    }

    public static Optional<BlocksizeSteppingOption> findByMailsPerHour(int mailsPerHour) {
        return Stream.of(BlocksizeSteppingOption.values())
                .filter(o -> o.getMailsPerHour() == mailsPerHour)
                .findAny();
    }

    public static Optional<BlocksizeSteppingOption> findByBlockSizeAndStepping(int blockSize, int stepping) {
        return Stream.of(BlocksizeSteppingOption.values())
                .filter(o -> o.getBlockSize() == blockSize && o.getStepping() == stepping)
                .findAny();
    }
}
