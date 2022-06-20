/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mailing.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map.Entry;

import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.service.ImportResult;
import org.agnitas.service.MailingExporter;
import org.agnitas.service.MailingImporter;
import org.agnitas.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class CopyMailingServiceImpl implements CopyMailingService {
	private static final Logger logger = LogManager.getLogger(CopyMailingService.class);
    
    private MailingExporter mailingExporter;
    
    private MailingImporter mailingImporter;
    
	@Override
	public int copyMailing(int sourceCompanyID, int sourceMailingID, int destinationCompanyID, String nameOfCopy, String descriptionOfCopy) throws Exception {
		File tempFile = null;
		try {
			tempFile = File.createTempFile("CopyMailing_", FileUtils.JSON_EXTENSION);
			try (FileOutputStream output = new FileOutputStream(tempFile)) {
				mailingExporter.exportMailingToJson(sourceCompanyID, sourceMailingID, output, false);
			}
			
			try (FileInputStream input = new FileInputStream(tempFile)) {
				// set importGridTemplateAllowed to true for copy, because permissions have to exists, when hit copy button
				// set checkIsTemplate to false for copy, because imported template to mailing
				ImportResult result = mailingImporter.importMailingFromJson(destinationCompanyID, input, false, nameOfCopy, descriptionOfCopy, true, false, true);
				
				if (result.isSuccess()) {
					if (result.getWarnings() != null) {
						for (Entry<String, Object[]> entry : result.getWarnings().entrySet()) {
							logger.warn("Copy mailing warning: " + entry.getKey() + ": " + entry.getValue());
						}
					}
					return result.getMailingID();
				} else {
					throw new Exception("Error while copy mailing");
				}
			}
		} finally {
			if (tempFile != null) {
				tempFile.delete();
			}
		}
	}

    @Required
    public void setMailingExporter(MailingExporter mailingExporter) {
        this.mailingExporter = mailingExporter;
    }

    @Required
    public void setMailingImporter(MailingImporter mailingImporter) {
        this.mailingImporter = mailingImporter;
    }
}
