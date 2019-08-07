/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailing;

import java.util.Collection;
import java.util.List;

import org.agnitas.beans.Mailing;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.mailing.service.MailingModel.Format;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.apache.log4j.Logger;

import com.agnitas.beans.MediatypeEmail;

public class ResponseBuilder {
	private static final transient Logger logger = Logger.getLogger(ResponseBuilder.class);
	
	private final ObjectFactory objectFactory;

	public ResponseBuilder(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}
	
	public org.agnitas.emm.springws.jaxb.Mailing createResponse(Mailing mailing) {
		org.agnitas.emm.springws.jaxb.Mailing responseMailing = objectFactory.createMailing();
		
		responseMailing.setMailingID(mailing.getId());
		responseMailing.setShortname(mailing.getShortname());
		responseMailing.setDescription(mailing.getDescription());
		responseMailing.setMailinglistID(mailing.getMailinglistID());
		Collection<Integer> targetGroups = mailing.getTargetGroups();
		if (targetGroups != null) {
			org.agnitas.emm.springws.jaxb.Mailing.TargetIDList targetIDList = objectFactory.createMailingTargetIDList();
			for (Integer targetID : targetGroups) {
				targetIDList.getTargetID().add(targetID);
			}
			responseMailing.setTargetIDList(targetIDList);
		}
		try {
			responseMailing.setMailingType(MailingModel.getMailingType(mailing.getMailingType()).getName());
		} catch (RuntimeException e) {
			responseMailing.setMailingType("Unknown");
		}
	
		MediatypeEmail emailParam = null;
		try {
			emailParam = mailing.getEmailParam();
		} catch (ClassCastException e) {
			logger.warn("Unable to retrieve emailParam from mailing[mailingId="+mailing.getId()+"]", e);
		}
		if (emailParam != null) {
			responseMailing.setSubject(emailParam.getSubject());
			responseMailing.setSenderName(emailParam.getFromFullname());
			responseMailing.setSenderAddress(emailParam.getFromEmail());
			responseMailing.setReplyToName(emailParam.getReplyFullname());
			responseMailing.setReplyToAddress(emailParam.getReplyEmail());
			responseMailing.setCharset(emailParam.getCharset());
			responseMailing.setLinefeed(emailParam.getLinefeed());
			List<Format> formatList = MailingModel.getFormatList(emailParam.getMailFormat());
			org.agnitas.emm.springws.jaxb.Mailing.Formats formats = objectFactory.createMailingFormats();
			for (Format format : formatList) {
				formats.getFormat().add(format.getName());
			}
			responseMailing.setFormats(formats);
			responseMailing.setOnePixel(emailParam.getOnepixel());
		}
		responseMailing.setAutoUpdate(mailing.getUseDynamicTemplate());
		
		return responseMailing;
	}

	public org.agnitas.emm.springws.jaxb.Template createTemplateResponse(Mailing mailing) {
		org.agnitas.emm.springws.jaxb.Template responseTemplate = objectFactory.createTemplate();
		
		responseTemplate.setTemplateID(mailing.getId());
		responseTemplate.setShortname(mailing.getShortname());
		responseTemplate.setDescription(mailing.getDescription());
		responseTemplate.setMailinglistID(mailing.getMailinglistID());
		Collection<Integer> targetGroups = mailing.getTargetGroups();
		if (targetGroups != null) {
			org.agnitas.emm.springws.jaxb.Template.TargetIDList targetIDList = objectFactory.createTemplateTargetIDList();
			for (Integer targetID : targetGroups) {
				targetIDList.getTargetID().add(targetID);
			}
			responseTemplate.setTargetIDList(targetIDList);
		}
		try {
			responseTemplate.setMailingType(MailingModel.getMailingType(mailing.getMailingType()).getName());
		} catch (RuntimeException e) {
			responseTemplate.setMailingType("Unknown");
		}
	
		MediatypeEmail emailParam = null;
		try {
			emailParam = mailing.getEmailParam();
		} catch (ClassCastException e) {
			logger.warn("Unable to retrieve emailParam from template[mailingId="+mailing.getId()+"]", e);
		} catch (NullPointerException e) {
			logger.warn("Unable to retrieve emailParam from template[mailingId="+mailing.getId()+"]", e);
		}
		if (emailParam != null) {
			responseTemplate.setSubject(emailParam.getSubject());
			responseTemplate.setSenderName(emailParam.getFromFullname());
			responseTemplate.setSenderAddress(emailParam.getFromEmail());
			responseTemplate.setReplyToName(emailParam.getReplyFullname());
			responseTemplate.setReplyToAddress(emailParam.getReplyEmail());
			responseTemplate.setCharset(emailParam.getCharset());
			responseTemplate.setLinefeed(emailParam.getLinefeed());
			List<Format> formatList = MailingModel.getFormatList(emailParam.getMailFormat());
			org.agnitas.emm.springws.jaxb.Template.Formats formats = objectFactory.createTemplateFormats();
			for (Format format : formatList) {
				formats.getFormat().add(format.getName());
			}
			responseTemplate.setFormats(formats);
			responseTemplate.setOnePixel(emailParam.getOnepixel());
		}
		responseTemplate.setAutoUpdate(mailing.getUseDynamicTemplate());
		
		return responseTemplate;
	}

}
