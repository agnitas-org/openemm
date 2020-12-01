package org.agnitas.service;

import org.agnitas.beans.ExportPredef;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.target.service.ComTargetService;

public class OpenemmRecipientExportWorkerFactory implements RecipientExportWorkerFactory {
	private ComTargetService targetService;

	@Override
	public RecipientExportWorker newWorker(ExportPredef exportProfile, ComAdmin admin) {
		return new RecipientExportWorker(exportProfile, admin, targetService);
	}

	@Required
	public void setTargetService(final ComTargetService targetService) {
		this.targetService = targetService;
	}
}
