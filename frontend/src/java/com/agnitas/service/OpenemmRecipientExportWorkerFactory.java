package com.agnitas.service;

import com.agnitas.beans.ExportPredef;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.target.service.TargetService;

public class OpenemmRecipientExportWorkerFactory implements RecipientExportWorkerFactory {
	public OpenemmRecipientExportWorkerFactory(
			TargetService targetService,
			RecipientFieldService recipientFieldService,
			MailinglistService mailinglistService) {
		this.targetService = targetService;
		this.recipientFieldService = recipientFieldService;
		this.mailinglistService = mailinglistService;
	}

	private TargetService targetService;
	private RecipientFieldService recipientFieldService;
	private MailinglistService mailinglistService;

	@Override
	public RecipientExportWorker newWorker(ExportPredef exportProfile, Admin admin) throws Exception {
		return new RecipientExportWorker(exportProfile, admin, targetService, recipientFieldService, mailinglistService);
	}
}
