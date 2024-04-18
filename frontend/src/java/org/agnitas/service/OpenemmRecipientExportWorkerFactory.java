package org.agnitas.service;

import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import org.agnitas.beans.ExportPredef;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.service.ColumnInfoService;

public class OpenemmRecipientExportWorkerFactory implements RecipientExportWorkerFactory {
	private ComTargetService targetService;
	private ColumnInfoService columnInfoService;
	private MailinglistService mailinglistService;

	@Override
	public RecipientExportWorker newWorker(ExportPredef exportProfile, Admin admin) throws Exception {
		return new RecipientExportWorker(exportProfile, admin, targetService, columnInfoService, mailinglistService);
	}

	@Required
	public void setTargetService(final ComTargetService targetService) {
		this.targetService = targetService;
	}

	@Required
	public void setColumnInfoService(final ColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}

	@Required
	public void setMailinglistService(MailinglistService mailinglistService) {
		this.mailinglistService = mailinglistService;
	}
}
