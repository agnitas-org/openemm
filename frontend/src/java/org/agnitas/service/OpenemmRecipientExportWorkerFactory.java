package org.agnitas.service;

import org.agnitas.beans.ExportPredef;

import com.agnitas.beans.ComAdmin;

public class OpenemmRecipientExportWorkerFactory implements RecipientExportWorkerFactory {
	
	@Override
	public final RecipientExportWorker newWorker(final ExportPredef exportProfile, final ComAdmin admin) {
		return new RecipientExportWorker(exportProfile, admin);
	}

}
