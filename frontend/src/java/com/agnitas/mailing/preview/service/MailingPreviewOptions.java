/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.preview.service;

public class MailingPreviewOptions {

	private int gridTemplateId;
	private int customerId;
	private boolean isForMobile;

	public static MailingPreviewOptions.Builder builder() {
		return new MailingPreviewOptions.Builder();
	}

	private MailingPreviewOptions() {
	}

	public int getGridTemplateId() {
		return gridTemplateId;
	}

	public int getCustomerId() {
		return customerId;
	}

	public boolean isForMobile() {
		return isForMobile;
	}

	public static class Builder {
		private int gridTemplateId;
		private int customerId;
		private boolean isForMobile;

		public MailingPreviewOptions.Builder setGridTemplateId(int gridTemplateId) {
			this.gridTemplateId = gridTemplateId;
			return this;
		}

		public MailingPreviewOptions.Builder setCustomerId(int customerId) {
			this.customerId = customerId;
			return this;
		}

		public MailingPreviewOptions.Builder setForMobile(boolean forMobile) {
			this.isForMobile = forMobile;
			return this;
		}

		public MailingPreviewOptions build() {
			MailingPreviewOptions options = new MailingPreviewOptions();

			options.gridTemplateId = gridTemplateId;
			options.customerId = customerId;
			options.isForMobile = isForMobile;

			return options;
		}
	}

}
