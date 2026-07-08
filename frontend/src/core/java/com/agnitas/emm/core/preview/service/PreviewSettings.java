/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.preview.service;

import com.agnitas.preview.ModeType;
import com.agnitas.preview.Preview;

public final class PreviewSettings {

    public static final int DEFAULT_FORMAT = MailingWebPreviewService.INPUT_TYPE_HTML;

    /**
     * Text, Html, ...
     */
    private final int format;

    private final boolean noImages;

    private final int customerId;

    private final int mailingId;

    private final int targetGroupId;

    private final Preview.Size previewSize;

    private final ModeType mode;

    private final boolean anonymous;

    private final boolean preserveLinksWhenAnonymous;

    private final String rdirDomain;

    public static class PreviewSettingsBuilder {

        private int format;
        private boolean noImages;
        private int customerId;
        private int mailingId;
        private int targetGroupId;
        private Preview.Size previewSize;
        private ModeType mode;
        private boolean anonymous;
        private boolean preserveLinksWhenAnonymous;
        private String rdirDomain;

        private PreviewSettingsBuilder() {
            this.format = PreviewSettings.DEFAULT_FORMAT;
        }

        public PreviewSettingsBuilder withPreviewFormat(final int format) {
            this.format = format;
            return this;
        }

        public PreviewSettingsBuilder withNoImages(final boolean noImages) {
            this.noImages = noImages;
            return this;
        }

        public PreviewSettingsBuilder withCustomerId(final int customerId) {
            this.customerId = customerId;
            return this;
        }

        public PreviewSettingsBuilder withMailingId(final int mailingId) {
            this.mailingId = mailingId;
            return this;
        }

        public PreviewSettingsBuilder withRdirDomain(String rdirDomain) {
            this.rdirDomain = rdirDomain;
            return this;
        }

        public PreviewSettingsBuilder withTargetGroupId(final int targetGroupId) {
            this.targetGroupId = targetGroupId;
            return this;
        }

        public PreviewSettingsBuilder withPreviewSize(final Preview.Size size) {
            this.previewSize = size != null ? size : Preview.Size.getDefaultSize();
            return this;
        }

        public PreviewSettingsBuilder withMode(final ModeType mode) {
            this.mode = mode != null ? mode : ModeType.getDefaultMode();
            return this;
        }

        public PreviewSettingsBuilder withAnonymous(final boolean anonymous, final boolean preserveLinks) {
            this.anonymous = anonymous;
            this.preserveLinksWhenAnonymous = preserveLinks;
            return this;
        }

        public final PreviewSettings build() {
            return new PreviewSettings(this);
        }
    }

    /*
    We are using the builder pattern here to ensure that PreviewSettings is immutable and no one had
    to take care of all properties of PreviewSettings.
     */
    private PreviewSettings(PreviewSettingsBuilder builder) {
        this.format = builder.format;
        this.noImages = builder.noImages;
        this.customerId = builder.customerId;
        this.mailingId = builder.mailingId;
        this.targetGroupId = builder.targetGroupId;
        this.previewSize = builder.previewSize;
        this.anonymous = builder.anonymous;
        this.preserveLinksWhenAnonymous = builder.preserveLinksWhenAnonymous;
        this.mode = builder.mode;
        this.rdirDomain = builder.rdirDomain;

        assert previewSize != null : "Preview size is null"; // Ensured by builder
        assert mode != null : "Mode is null"; // Ensured by builder
    }

    public static PreviewSettingsBuilder builder() {
        return new PreviewSettingsBuilder();
    }

    public int getFormat() {
        return format;
    }

    public boolean isNoImages() {
        return noImages;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getRdirDomain() {
        return rdirDomain;
    }

    public Preview.Size getPreviewSize() {
        return previewSize;
    }

    public ModeType getMode() {
        return mode;
    }

    public int getMailingId() {
        return mailingId;
    }

    public int getTargetGroupId() {
        return targetGroupId;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public boolean isPreserveLinksWhenAnonymous() {
        return preserveLinksWhenAnonymous;
    }
}
