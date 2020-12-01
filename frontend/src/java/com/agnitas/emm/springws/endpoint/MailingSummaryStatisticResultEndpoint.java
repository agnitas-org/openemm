/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.stat.beans.MailingStatJobDescriptor;
import com.agnitas.emm.core.stat.beans.MailingStatisticTgtGrp;
import com.agnitas.emm.core.stat.beans.StatisticValue;
import com.agnitas.emm.core.stat.service.MailingSummaryStatisticJobService;
import com.agnitas.emm.core.stat.service.impl.TargetGroupsStringFormatException;
import com.agnitas.emm.springws.jaxb.GroupStatisticInfo;
import com.agnitas.emm.springws.jaxb.MailingSummaryStatisticResultRequest;
import com.agnitas.emm.springws.jaxb.MailingSummaryStatisticResultResponse;
import com.agnitas.emm.springws.jaxb.MailingSummaryStatisticResultResponse.Items;
import com.agnitas.emm.springws.jaxb.StatisticEntry;
import com.agnitas.reporting.birt.external.dataset.CommonKeysService;

@Endpoint
public class MailingSummaryStatisticResultEndpoint extends BaseEndpoint {
	private static final Logger classLogger = Logger.getLogger(MailingSummaryStatisticJobEndpoint.class);

	private MailingSummaryStatisticJobService mailingSummaryStatisticJobService;

	public MailingSummaryStatisticResultEndpoint(MailingSummaryStatisticJobService mailingSummaryStatisticJobService) {
		this.mailingSummaryStatisticJobService = mailingSummaryStatisticJobService;
	}

	@PayloadRoot(namespace = Utils.NAMESPACE_COM, localPart = "MailingSummaryStatisticResultRequest")
	public @ResponsePayload MailingSummaryStatisticResultResponse mailingSummaryStatisticResult(@RequestPayload MailingSummaryStatisticResultRequest request) throws Exception {
		if (classLogger.isInfoEnabled()) {
			classLogger.info( "Entered MailingSummaryStatisticResultEndpoint.mailingSummaryStatisticResult()");
		}
		
		MailingSummaryStatisticResultResponse response = new MailingSummaryStatisticResultResponse();

		MailingStatJobDescriptor job = mailingSummaryStatisticJobService.getStatisticJob(request.getStatisticJobID());
		response.setStatisticJobStatus(job.getStatus());
		response.setStatisticJobStatusDescription(job.getStatusDescription());
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(job.getCreationDate());
		XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		response.setTimestamp(date);
		
		if (job.getStatus() == MailingStatJobDescriptor.STATUS_SUCCEED) {
			response.setItems(new Items());
			addGroupStat(job.getId(), 0, response); // use target group ID = 0 for 'all subscribers'
			
			if (StringUtils.isNotBlank(job.getTargetGroups())) {
				try {
					List<Integer> targetGroups = mailingSummaryStatisticJobService.parseGroupList(job.getTargetGroups());
					for (Integer targetGroup : targetGroups) {
						addGroupStat(job.getId(), targetGroup, response);
					}
				} catch (NumberFormatException e) {
					throw new TargetGroupsStringFormatException();
				}
			}
		}
		
		if (classLogger.isInfoEnabled()) {
			classLogger.info( "Leaving MailingSummaryStatisticResultEndpoint.mailingSummaryStatisticResult()");
		}
		
		return response;
	}
	
	private void addGroupStat(int jobId, int targetGroupId, MailingSummaryStatisticResultResponse response) throws DataAccessException {
		GroupStatisticInfo groupStat = new GroupStatisticInfo();
		groupStat.setTargetGroupId(targetGroupId);
		
		GroupStatisticInfo.Items items = new GroupStatisticInfo.Items();
		groupStat.setItems(items);
		
		try {
			MailingStatisticTgtGrp block = mailingSummaryStatisticJobService.getStatisticTgtGrp(jobId, targetGroupId);
			for (Entry<Integer, StatisticValue> entry : block.getStatValues().entrySet()) {
				StatisticEntry stEntry = new StatisticEntry();
				stEntry.setToken(CommonKeysService.tokenByIndex(entry.getKey()));
				stEntry.setValue(beanToJax(entry.getValue()));
				items.getValue().add(stEntry);
			}
			
			response.getItems().getItem().add(groupStat);
		} catch (DataAccessException e) {
			classLogger.error("Failed statistic for targetGroupId " + targetGroupId, e);
			if (targetGroupId == 1) {
				// Failed with statistic of all recipients
				throw e;
			}
		}
	}

	private com.agnitas.emm.springws.jaxb.StatisticValue beanToJax(StatisticValue stat) {
		if (stat == null || stat.getValue() < 0 || stat.getQuotient() < 0) {
			return null;
		}
		
		com.agnitas.emm.springws.jaxb.StatisticValue jaxStat = new com.agnitas.emm.springws.jaxb.StatisticValue();
		jaxStat.setValue(stat.getValue());
		jaxStat.setQuotient(new BigDecimal(stat.getQuotient()));
		return jaxStat;
	}
}
