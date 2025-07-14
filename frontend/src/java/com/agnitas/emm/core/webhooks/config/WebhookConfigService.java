/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.config;

import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;

/**
 * Wrapper around {@link ConfigService} providing only
 * relevant configuration.
 */
public final class WebhookConfigService {

	/** Wrapped ConfigService. */
	private final ConfigService configService;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param service {@link ConfigService} to wrap
	 * 
	 * @throws NullPointerException if given {@link ConfigService} is <code>null</code>
	 */
	public WebhookConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}
	
	/**
	 * Checks, if webhook interface is enabled for given company ID.
	 * 
	 * @param companyID company ID
	 * 
	 * @return <code>true</code> if webhook interface is enabled
	 */
	public final boolean isWebhookInterfaceEnabled(final int companyID) {
		return configService.getBooleanValue(ConfigValue.Webhooks.WebhooksEnabled, companyID);
	}

	/**
	 * Returns the maximum number of retries.
	 * 
	 * @param companyId company ID
	 * 
	 * @return maximum number of retries
	 */
	public final int getMaximumRetryCount(final int companyId) {
		return configService.getIntegerValue(ConfigValue.Webhooks.MaximumRetryCount, companyId);
	}

	/**
	 * Returns the delay (in seconds) for next retry.
	 * 
	 * @param companyId company ID
	 * 
	 * @return delay (in seconds) for next retry
	 */
	public final int getRetryDelaySeconds(final int companyId) {
		return configService.getIntegerValue(ConfigValue.Webhooks.RetryDelaySeconds, companyId);
	}

	/**
	 * Returns the connect timeout in milliseconds.
	 * 
	 * @param companyId company ID
	 * 
	 * @return connect timeout in milliseconds
	 */
	public final int getConnectTimeoutMillis(final int companyId) {
		return configService.getIntegerValue(ConfigValue.Webhooks.ConnectTimeoutMillis, companyId);
	}

	/**
	 * Returns the socket timeout in milliseconds.
	 * 
	 * @param companyId company ID
	 * 
	 * @return socket timeout in milliseconds
	 */
	public final int getSocketTimeoutMillis(final int companyId) {
		return configService.getIntegerValue(ConfigValue.Webhooks.SocketTimeoutMillis, companyId);
	}

	public final int getMessageRetentionSeconds() {
		return configService.getIntegerValue(ConfigValue.Webhooks.MessageRetentionTimeSeconds);
	}
	
	public final int getMessageGenerationGracePeriodSeconds(final int companyID) {
		return configService.getIntegerValue(ConfigValue.Webhooks.MessageGenerationGracePeriodSeconds, companyID);
	}

	public void enableWebhooksInterface(final int companyID, final boolean enable) {
		this.configService.writeOrDeleteIfDefaultBooleanValue(
				ConfigValue.Webhooks.WebhooksEnabled, 
				companyID, 
				enable,
				String.format("Webhook interfaced %s", enable ? "enabled" : "disabled"));
	}
	
}
