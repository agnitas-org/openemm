/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map.Entry;

import com.agnitas.emm.core.mailing.exception.MailingCopyException;
import com.agnitas.emm.core.mailing.service.CopyMailingService;
import com.agnitas.service.ImportResult;
import com.agnitas.service.MailingExporter;
import com.agnitas.service.MailingImporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CopyMailingServiceImpl implements CopyMailingService {

	private static final Logger logger = LogManager.getLogger(CopyMailingServiceImpl.class);
    
    private final MailingExporter mailingExporter;
    private final MailingImporter mailingImporter;

    public CopyMailingServiceImpl(MailingExporter mailingExporter, MailingImporter mailingImporter) {
        this.mailingExporter = mailingExporter;
        this.mailingImporter = mailingImporter;
    }

    @Override
	public int copyMailing(int sourceCompanyID, int sourceMailingID, int destinationCompanyID, String nameOfCopy, String descriptionOfCopy) {
		try {
			byte[] data = mailingExporter.exportAsJson(sourceMailingID, sourceCompanyID, false, true);

			try (InputStream input = new ByteArrayInputStream(data)) {
				// set importGridTemplateAllowed to true for copy, because permissions have to exists, when hit copy button
				// set checkIsTemplate to false for copy, because imported template to mailing
				ImportResult result = mailingImporter.importMailingFromJson(destinationCompanyID, input, false, nameOfCopy, descriptionOfCopy, true, false, true);
				
				if (result.isSuccess()) {
					if (result.getWarnings() != null) {
						for (Entry<String, Object[]> entry : result.getWarnings().entrySet()) {
							logger.warn("Copy mailing warning: {}: {}", entry.getKey(), entry.getValue());
						}
					}
					return result.getMailingID();
				}
			}
		} catch (Exception e) {
			throw new MailingCopyException("Error while copy mailing: " + sourceMailingID, e);
		}

		throw new MailingCopyException("Error while copy mailing: " + sourceMailingID);
	}

}
