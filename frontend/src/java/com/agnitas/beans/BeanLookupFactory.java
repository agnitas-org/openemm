/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.Mediatype;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.TrackableLink;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.velocity.VelocityWrapperFactory;

import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.workflow.dao.ComWorkflowReportScheduleDao;
import com.agnitas.util.ScriptHelper;

public abstract class BeanLookupFactory {
	abstract public Mailing getBeanMailing();
	abstract public TrackableLink getBeanTrackableLink();
	abstract public MailingComponent getBeanMailingComponent();
	abstract public DynamicTag getBeanDynamicTag();
	abstract public DynamicTagContent getBeanDynamicTagContent();
	abstract public Mediatype getBeanMediatypeEmail();
	abstract public Recipient getBeanRecipient();
	abstract public VelocityWrapperFactory getBeanVelocityWrapperFactory();
	abstract public DatasourceDescription getBeanDatasourceDescription();
	abstract public ExtensibleUIDService getBeanExtensibleUIDService();
	abstract public BindingEntry getBeanBindingEntry();
	abstract public ScriptHelper getBeanScriptHelper();
	
	@Deprecated // TODO Seems to be not used. (If used: Switch to TargetFactory)
	abstract public ComTarget getBeanTarget();
	abstract public Mailinglist getBeanMailinglist();
	abstract public JavaMailService getBeanJavaMailService();
	abstract public ConfigService getBeanConfigService();
	abstract public ComWorkflowReportScheduleDao getBeanWorkflowReportScheduleDao();
}
