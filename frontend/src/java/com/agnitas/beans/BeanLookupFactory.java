/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.DatasourceDescription;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.Recipient;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.velocity.VelocityWrapperFactory;

import com.agnitas.emm.core.JavaMailService;
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
	abstract public Mailinglist getBeanMailinglist();
	abstract public JavaMailService getBeanJavaMailService();
	abstract public ConfigService getBeanConfigService();
}
