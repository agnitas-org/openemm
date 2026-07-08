/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.LicenseParser;
import com.agnitas.emm.core.velocity.VelocityWrapperFactory;
import com.agnitas.util.ScriptHelper;

public abstract class BeanLookupFactory {

    public abstract Mailing getBeanMailing();

    public abstract TrackableLink getBeanTrackableLink();

    public abstract MailingComponent getBeanMailingComponent();

    public abstract DynamicTag getBeanDynamicTag();

    public abstract DynamicTagContent getBeanDynamicTagContent();

    public abstract Mediatype getBeanMediatypeEmail();

    public abstract Recipient getBeanRecipient();

    public abstract VelocityWrapperFactory getBeanVelocityWrapperFactory();

    public abstract DatasourceDescription getBeanDatasourceDescription();

    public abstract ExtensibleUIDService getBeanExtensibleUIDService();

    public abstract BindingEntry getBeanBindingEntry();

    public abstract ScriptHelper getBeanScriptHelper();

    public abstract Mailinglist getBeanMailinglist();

    public abstract JavaMailService getBeanJavaMailService();

    public abstract ConfigService getBeanConfigService();

    public abstract LicenseParser getBeanLicenseParser();

}
