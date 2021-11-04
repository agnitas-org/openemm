package org.agnitas.service;

import org.agnitas.beans.ExportPredef;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.service.ComColumnInfoService;

public class OpenemmRecipientExportWorkerFactory implements RecipientExportWorkerFactory {
	private ComTargetService targetService;
	private ComColumnInfoService columnInfoService;

	@Override
	public RecipientExportWorker newWorker(ExportPredef exportProfile, ComAdmin admin) throws Exception {
		return new RecipientExportWorker(exportProfile, admin, targetService, columnInfoService);
	}

	@Required
	public void setTargetService(final ComTargetService targetService) {
		this.targetService = targetService;
	}

	@Required
	public void setColumnInfoService(final ComColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}
}
