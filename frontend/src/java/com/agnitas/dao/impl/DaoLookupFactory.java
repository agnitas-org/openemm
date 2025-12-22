/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import javax.sql.DataSource;

import com.agnitas.dao.AnonymizeStatisticsDao;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.CssDao;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.dao.ImportRecipientsDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.RdirTrafficAmountDao;
import com.agnitas.dao.RecipientDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.dao.UndoDynContentDao;
import com.agnitas.dao.UndoMailingComponentDao;
import com.agnitas.dao.UndoMailingDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.imports.reporter.ProfileImportReporter;
import com.agnitas.emm.core.loginmanager.dao.LoginTrackDao;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import com.agnitas.emm.core.workflow.dao.WorkflowReactionDao;
import com.agnitas.service.ImportModeHandlerFactory;

public abstract class DaoLookupFactory {
	
	abstract public DataSource getBeanDataSource();
	abstract public DatasourceDescriptionDao getBeanDatasourceDescriptionDao();
	abstract public CompanyDao getBeanCompanyDao();
	abstract public DynamicTagDao getBeanDynamicTagDao();
	abstract public MailingDao getBeanMailingDao();
	abstract public MaildropService getBeanMaildropService();
	abstract public LoginTrackDao getBeanGuiLoginTrackDao();
	abstract public LoginTrackDao getBeanWsLoginTrackDao();
	abstract public UndoMailingDao getBeanUndoMailingDao();
	abstract public UndoMailingComponentDao getBeanUndoMailingComponentDao();
	abstract public UndoDynContentDao getBeanUndoDynContentDao();
	abstract public TargetDao getBeanTargetDao();
	abstract public MailinglistDao getBeanMailinglistDao();
	abstract public ImportRecipientsDao getBeanImportRecipientsDao();
	abstract public WorkflowReactionDao getBeanWorkflowReactionDao();
	abstract public RecipientDao getBeanRecipientDao();
	abstract public EmmActionService getBeanEmmActionService();
	abstract public ProfileImportReporter getBeanProfileImportReporter();
	abstract public ImportModeHandlerFactory getBeanImportModeHandlerFactory();
	abstract public RdirTrafficAmountDao getBeanRdirTrafficAmountDao();
	abstract public JavaMailService getBeanJavaMailService();
	abstract public AnonymizeStatisticsDao getBeanAnonymizeStatisticsDao();
	abstract public CssDao getBeanCssDao();
}
