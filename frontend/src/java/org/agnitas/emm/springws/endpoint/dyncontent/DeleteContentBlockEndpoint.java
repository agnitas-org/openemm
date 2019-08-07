/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.dyncontent;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.agnitas.emm.core.dyncontent.service.ContentModel;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.DeleteContentBlockRequest;
import org.agnitas.emm.springws.jaxb.DeleteContentBlockResponse;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.agnitas.service.UserActivityLogService;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

@SuppressWarnings("deprecation")
public class DeleteContentBlockEndpoint extends AbstractMarshallingPayloadEndpoint {

	@Resource
	private DynamicTagContentService dynamicTagContentService;
	@Resource
	private ObjectFactory objectFactory;
	@Resource
	private UserActivityLogService userActivityLogService;

	@Override
	protected Object invokeInternal(Object arg0) throws Exception {
		DeleteContentBlockRequest request = (DeleteContentBlockRequest) arg0;
		DeleteContentBlockResponse response = objectFactory.createDeleteContentBlockResponse();

		ContentModel model = new ContentModel();
		model.setCompanyId(Utils.getUserCompany());
		model.setContentId(request.getContentID());

		List<UserAction> userActions = new ArrayList<>();
		response.setValue(dynamicTagContentService.deleteContent(model, userActions));
		Utils.writeLog(userActivityLogService, userActions);

		return response;
	}

}
