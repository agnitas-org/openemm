/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.preview.dto;

import java.util.Objects;
import java.util.Optional;

public final class PreviewResult {

    private final int previewFormat;
    private final Optional<String> previewContent;
    private final String smsNumber;

    public PreviewResult(final int previewFormat, final Optional<String> previewContent) {
        this(previewFormat, previewContent, null);
    }

    public PreviewResult(final int previewFormat, final Optional<String> previewContent, final String smsNumber) {
        this.previewFormat = previewFormat;
        this.previewContent = Objects.requireNonNull(previewContent, "optional preview content");
        this.smsNumber = smsNumber; // Can be null;
    }

    public int getPreviewFormat() {
        return previewFormat;
    }

    public String getSmsNumber() {
        return smsNumber;
    }

    public Optional<String> getPreviewContent() {
        return previewContent;
    }

}
