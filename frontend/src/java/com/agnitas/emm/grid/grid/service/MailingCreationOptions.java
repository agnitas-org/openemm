/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

public final class MailingCreationOptions {
    public static Builder builder() {
        return new Builder();
    }

    private boolean alwaysCreateNew;
    private int mailingListId;
    private String targetGroupExtension;
    private boolean createdFromReleased;
    private String textTemplateStub;
    private boolean generateTextVersion;

    private MailingCreationOptions() {}

    public boolean isAlwaysCreateNew() {
        return alwaysCreateNew;
    }

    public int getMailingListId() {
        return mailingListId;
    }

    public String getTargetGroupExtension() {
        return targetGroupExtension;
    }

    public boolean isCreatedFromReleased() {
        return createdFromReleased;
    }

    public String getTextTemplateStub() {
        return textTemplateStub;
    }

    public boolean isGenerateTextVersion() {
        return generateTextVersion;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MailingCreationOptions) {
            MailingCreationOptions options = (MailingCreationOptions) obj;

            return options.alwaysCreateNew == alwaysCreateNew &&
                options.mailingListId == mailingListId &&
                StringUtils.equals(options.targetGroupExtension, targetGroupExtension) &&
                options.createdFromReleased == createdFromReleased &&
                StringUtils.equals(options.textTemplateStub, textTemplateStub) &&
                options.generateTextVersion == generateTextVersion;
        }

        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("alwaysCreateNew", alwaysCreateNew)
            .append("mailingListId", mailingListId)
            .append("targetGroupExtension", targetGroupExtension)
            .append("createdFromReleased", createdFromReleased)
            .append("textTemplateStub", textTemplateStub)
            .append("generateTextVersion", generateTextVersion)
            .toString();
    }

    public static class Builder {
        private MailingCreationOptions options = new MailingCreationOptions();

        private Builder() {}

        public Builder setMailingListId(int mailingListId) {
            options.mailingListId = mailingListId;
            return this;
        }

        public Builder setAlwaysCreateNew(boolean alwaysCreateNew) {
            options.alwaysCreateNew = alwaysCreateNew;
            return this;
        }

        public Builder setTargetGroupExtension(String targetGroupExtension) {
            options.targetGroupExtension = targetGroupExtension;
            return this;
        }

        public Builder setCreatedFromReleased(boolean createdFromReleased) {
            options.createdFromReleased = createdFromReleased;
            return this;
        }

        public Builder setTextTemplateStub(String textTemplateStub) {
            options.textTemplateStub = textTemplateStub;
            return this;
        }

        public Builder setGenerateTextVersion(boolean generateTextVersion) {
            options.generateTextVersion = generateTextVersion;
            return this;
        }

        public MailingCreationOptions build() {
            MailingCreationOptions result = options;
            options = null;
            return result;
        }
    }
}
