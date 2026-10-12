/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.preview.dto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.agnitas.emm.core.preview.model.RcsLink;

public final class PreviewResult {

    private final int previewFormat;
    private final String previewContent;
    private final String rcsImageUrl;
    private final List<RcsLink> rcsLinks;

    public PreviewResult(int previewFormat, String previewContent) {
        this(previewFormat, previewContent, "", Collections.emptyList());
    }

    public PreviewResult(int previewFormat, String previewContent, String rcsImageUrl, List<RcsLink> rcsLinks) {
        this.previewFormat = previewFormat;
        this.previewContent = previewContent;
        this.rcsLinks = rcsLinks;
        this.rcsImageUrl = rcsImageUrl;
    }

    public int getPreviewFormat() {
        return previewFormat;
    }

    public List<RcsLink> getRcsLinks() {
        return rcsLinks;
    }

    public String getRcsImageUrl() {
        return rcsImageUrl;
    }

    public Optional<String> getPreviewContent() {
        return Optional.ofNullable(previewContent);
    }

}
