/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.emm.common.MailingType;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.beans.factory.impl.MailingComponentFactoryImpl;
import org.agnitas.dao.impl.mapper.DateRowMapper;
import org.agnitas.util.SafeString;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.Campaign;
import com.agnitas.dao.impl.CampaignDaoImpl;
import com.agnitas.dao.impl.ComMailingComponentDaoImpl;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.LightMailing;
import com.agnitas.reporting.birt.external.beans.LightMailingList;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.dao.impl.LightMailingDaoImpl;
import com.agnitas.reporting.birt.external.dao.impl.LightMailingListDaoImpl;
import com.agnitas.reporting.birt.external.dataset.MailingBouncesDataSet.BouncesRow;

public class MailingDataSet extends BIRTDataSet {

	private static final Logger logger = LogManager.getLogger(MailingDataSet.class);
	
	public static class MailingData {
        int mailingId;
		String mailingName;
        String description;
		String subject;
		String senderName;
		String replyName;
		Long numMails;
		boolean isMailTrackingAvailable;
		int numberOfAnonymousUsers;
		String startSending;
		String stopSending;
		String scheduledDate;
		String mailFormat;
		Long averageMailsize;
        String mailingList;
		String archiveName;
        String targets;
		byte[] thumbnail;
		String evaluationPeriod;
		String dispatchTime;

		public String getMailingName() {
			return mailingName;
		}
		public String getSubject() {
			return subject;
		}
		public String getSenderName() {
			return senderName;
		}
		public String getReplyName() {
			return replyName;
		}
		public Long getNumMails() {
			return numMails;
		}
		public boolean isMailTrackingAvailable() {
			return isMailTrackingAvailable;
		}
		public int getNumberOfAnonymousUsers() {
			return numberOfAnonymousUsers;
		}
		public String getStartSending() {
			return startSending;
		}
		public String getStopSending() {
			return stopSending;
		}
		public String getScheduledDate() {
			return scheduledDate;
		}
		public String getMailFormat() {
			return mailFormat;
		}
		public Long getAverageMailsize() {
			return averageMailsize;
		}
        public int getMailingId() {
            return mailingId;
        }

        public String getDescription() {
            return description;
        }

        public String getMailingList() {
            return mailingList;
        }

        public String getArchiveName() {
            return archiveName;
        }

        public String getTargets() {
            return targets;
        }

        public void setTargets(String targets) {
            this.targets = targets;
        }

		public byte[] getThumbnail() {
			return thumbnail;
		}

		public String getEvaluationPeriod() {
			return evaluationPeriod;
		}

		public String getDispatchTime() {
			return dispatchTime;
		}
	}

	public List<MailingData> getData(Integer mailingId, Integer companyId, String language,
            String startDate, String stopDate, Boolean hourScale) throws Exception {
		Locale locale = new Locale(language);
        SimpleDateFormat dateTimeFormat = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
		dateTimeFormat.applyPattern(dateTimeFormat.toPattern().replaceFirst("y+", "yyyy").replaceFirst(", ", " "));
		MailingData data = new MailingData();
		LightMailing mailing = getMailing(mailingId, companyId);
		String mtParam = getMailingMtParam(mailingId);

        data.mailingId = mailingId;
        data.mailingName = mailing.getShortname();
        data.description = mailing.getDescription();
        data.mailingList = getMailingList(companyId, mailing.getMailinglistId()).getShortname();
		data.thumbnail = getThumbnailImage(mailingId, companyId);

        if (mtParam.isEmpty()) {
            String locMessage = SafeString.getLocaleString("mailing.NoEmailDataAvailable", locale);
            data.subject =  locMessage;
			data.senderName = locMessage;
			data.replyName = locMessage;
            data.mailFormat = locMessage;
        } else {
            data.subject = getSubject(mtParam);
			data.senderName = com.agnitas.reporting.birt.external.utils.StringUtils.findParam("from", mtParam);
			data.replyName = com.agnitas.reporting.birt.external.utils.StringUtils.findParam("reply", mtParam);
            try {
                data.mailFormat = I18nString.getLocaleString("MailType."+getMailformat(mtParam), language);
            } catch (Exception ex) {
                data.mailFormat = "-";
            }
        }

        DateFormats dateFormats = new DateFormats(startDate, stopDate, hourScale);

        if (mailing.getMailingType() == MailingType.DATE_BASED.getCode() || mailing.getMailingType() == MailingType.ACTION_BASED.getCode()
				|| mailing.getMailingType() == MailingType.INTERVAL.getCode()) {

			data.evaluationPeriod = String.format(
					"%s - %s",
					dateTimeFormat.format(dateFormats.getStartDateAsDate()),
					dateTimeFormat.format(dateFormats.getStopDateAsDate())
			);

			Date dispatchDate = getFirstDispatchDate(mailingId, companyId);
			data.dispatchTime = dispatchDate == null ? "-" : dateTimeFormat.format(dispatchDate);
		}

		Map<String,Object> mailStats = getMailingStats(mailingId, dateFormats.getStartDate(), dateFormats.getStopDate());
		data.numMails = ((Number) mailStats.get("MAILS")).longValue();
		data.isMailTrackingAvailable = isTrackingAvailableForMailing(mailingId, companyId);
		if (data.isMailTrackingAvailable) {
			data.numberOfAnonymousUsers =  getNumberOfAnonymousUsersInMailing(mailingId, companyId);
		}

		try {
            if (dateFormats.isDateSlice()) {
                data.startSending = dateTimeFormat.format(dateFormats.getStartDateAsDate());
            } else {
                data.startSending = dateTimeFormat.format(mailStats.get("MINTIME"));
            }
		} catch (Exception ex) {
			data.startSending = "-";
		}
		try {
            if (dateFormats.isDateSlice()) {
                data.stopSending = dateTimeFormat.format(dateFormats.getStopDateAsDate());
            } else {
                data.stopSending = dateTimeFormat.format(mailStats.get("MAXTIME"));
            }
		} catch (Exception ex) {
			data.stopSending = "-";
		}
		try {
			Object scheduledSendDate = getScheduledSendTime(mailingId);
			data.scheduledDate = scheduledSendDate == null ? "-" : dateTimeFormat.format(scheduledSendDate);
		} catch (Exception ex) {
			data.scheduledDate = "-";
		}
		try {
			long numberOfBytes = 0L;
			
			long numberOfSentEmails = 0L;
			if (mailStats.get("MAILS") != null) {
				numberOfSentEmails = ((Number) mailStats.get("MAILS")).longValue();
			}

			if (mailStats.get("BYTES") != null) {
				long dataAmountSentByBackend = ((Number) mailStats.get("BYTES")).longValue();
				numberOfBytes += dataAmountSentByBackend;
			}
			
			Map<String,Object> trafficAgrStats = selectSingleRow(logger, "SELECT SUM(content_size * amount) AS bytes FROM rdir_traffic_agr_" + companyId + "_tbl WHERE mailing_id = ?", mailingId);
			if (trafficAgrStats.get("BYTES") != null) {
				long dataAmountRequestedFromRdirHistoric = ((Number) trafficAgrStats.get("BYTES")).longValue();
				numberOfBytes += dataAmountRequestedFromRdirHistoric;
			}
			
			Map<String,Object> trafficStats = selectSingleRow(logger, "SELECT SUM(content_size) AS bytes FROM rdir_traffic_amount_" + companyId + "_tbl WHERE mailing_id = ?", mailingId);
			if (trafficStats.get("BYTES") != null) {
				long dataAmountRequestedFromRdirCurrentDay = ((Number) trafficStats.get("BYTES")).longValue();
				numberOfBytes += dataAmountRequestedFromRdirCurrentDay;
			}
			
			// averageMailsize is in kilobytes
			if (numberOfSentEmails > 0) {
				data.averageMailsize = numberOfBytes / numberOfSentEmails / 1024;
			} else {
				data.averageMailsize = 0L;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			data.averageMailsize = 0L;
		}
        try{
            Campaign campaign = getCampaign(mailing.getArchiveId(), companyId);
            data.archiveName = campaign.getShortname();
        } catch (Exception e) {
            data.archiveName = "-";
        }

        List<String> targets = getTargets(mailingId, companyId);
        if (targets.size() == 1 && "All_Subscribers".equals(targets.get(0))) {
			targets.clear();
        }

        String targetsStr = "";
        for (int i = 0; i < targets.size(); i++) {
            targetsStr += targets.get(i);
            if (i < targets.size() - 1) {
                targetsStr += "\n";
            }
        }
        data.setTargets(targetsStr);

        return Collections.singletonList(data);
	}

	private byte[] getThumbnailImage(int mailingId, int companyId) {
		ComMailingComponentDaoImpl componentDao = new ComMailingComponentDaoImpl();
		componentDao.setDataSource(getDataSource());
		componentDao.setMailingComponentFactory(new MailingComponentFactoryImpl());

		int componentId = componentDao.getImageComponent(companyId, mailingId, MailingComponentType.ThumbnailImage);
		MailingComponent component = componentDao.getMailingComponent(componentId, companyId);
		if (component != null) {
			return component.getBinaryBlock();
		}
		return null;
	}

	private Date getFirstDispatchDate(int mailingId, int companyId) {
		String query = "SELECT MIN(timestamp) FROM mailing_account_tbl WHERE company_id = ? AND mailing_id = ?";
		return selectObjectDefaultNull(logger, query, DateRowMapper.INSTANCE, companyId, mailingId);
	}

    public List<MailingDataSet.MailingData> getMailingsInfo(int companyID, String mailings, String language, DateFormats dateFormats) throws Exception {
        List<Integer> mailingIds = parseMailingIds(mailings);

        List<MailingDataSet.MailingData> mailingDataList = new LinkedList<>();
        for (Integer mailingId : mailingIds) {
            mailingDataList.addAll(getData(mailingId, companyID, language, dateFormats.getStartDate(), dateFormats.getStopDate(), dateFormats.isHourScale()));
        }
        return mailingDataList;
    }

	public LightMailing getMailing(Integer mailingId, Integer companyId) {
		LightMailing mailing = new LightMailingDaoImpl(getDataSource()).getMailing(mailingId, companyId);
		if (mailing == null) {
			return new LightMailing();
		}
		
		return mailing;
	}

	public Campaign getCampaign(Integer campaignId, Integer companyId) {
		CampaignDaoImpl campaignDaoImpl = new CampaignDaoImpl();
		campaignDaoImpl.setDataSource(getDataSource());
		return campaignDaoImpl.getCampaign(campaignId, companyId);
	}

    private LightMailingList getMailingList(int companyId, int mailingListId) {
		LightMailingList mailingList = new LightMailingListDaoImpl(getDataSource()).getMailingList(mailingListId, companyId);
		if (mailingList == null) {
			return new LightMailingList();
		}
		return mailingList;
	}

    public String getMailingMtParam(Integer mailingId) {
    	List<Map<String,Object>> result = select(logger, "SELECT param FROM mailing_mt_tbl WHERE mailing_id = ? AND mediatype = 0 AND status = 2", mailingId);
    	if (result.size() == 1) {
    		return (String) result.get(0).get("param");
    	} else {
    		return "";
    	}
	}

	public String getSubject(String mtParam) {
        String subject = com.agnitas.reporting.birt.external.utils.StringUtils.findParam("subject", mtParam);
        subject = (null != subject) ? subject : "";
        return subject;
	}

	public String getMailformat(String mtParam) {
		Matcher mat = Pattern.compile("mailformat=\"(.*?)\"").matcher(mtParam);
		mat.find();
		String mailformat = mat.group(1);
		return (null != mailformat) ? mailformat : "";
	}
	
	public List<String> getTargets(int mailingID, int companyID ) {
		List<String> targets = new ArrayList<>();
		LightMailing mailing = getMailing(mailingID, companyID);
		String targExp = mailing.getTargetExpression();

		if (StringUtils.isEmpty(targExp) || "null".equals(targExp.trim())) {
			 targets.add("All_Subscribers");
		} else {
			targExp = targExp.replace('&',',');
			targExp = targExp.replace('|',',');
            targExp = targExp.replace("(","");
            targExp = targExp.replace(")","");
            targExp = targExp.replace("!","");
            targExp = targExp.trim();
			List<LightTarget> list = getTargets(Arrays.asList(targExp.split(",")), companyID);
			for (LightTarget lt : list) {
				targets.add(lt.getName());
			}
		}
		return targets;
	}

	public Map<String, Object> getMailingStats(Integer mailingID, String startDateString, String endDateString) throws Exception {
		List<Object> parameters = new ArrayList<>();
		
		StringBuilder queryBuilder = new StringBuilder()
			.append("SELECT")
			.append(" ").append("COALESCE(SUM(no_of_mailings), 0) AS MAILS,")
			.append(" MIN(timestamp) AS MINTIME,")
			.append(" MAX(timestamp) AS MAXTIME,")
			.append(" ").append("COALESCE(SUM(no_of_bytes), 0) AS BYTES")
			.append(" FROM mailing_account_tbl")
			.append(" WHERE mailing_id = ?")
			.append(" AND status_field NOT IN ('A', 'T', 'V')");
		parameters.add(mailingID);
		
		if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
			queryBuilder.append(" AND (? <= timestamp AND timestamp < ?)");
			if (startDateString.contains(":")) {
				parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
			} else {
				parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
			}
			if (endDateString.contains(":")) {
				parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
			} else {
				parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
			}
		}

		return selectSingleRow(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));
	}

	private int getNumberOfAnonymousUsersInMailing(int mailingId, int companyId) {
		String mailTrackTableName = String.format("mailtrack_%d_tbl", companyId);
		String customerTableName = String.format("customer_%d_tbl", companyId);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT COUNT(DISTINCT cust.customer_id)");
		queryBuilder.append(String.format(" FROM %s mtrack", mailTrackTableName));
		queryBuilder.append(String.format(" JOIN %s cust ON cust.customer_id = mtrack.customer_id", customerTableName));
		queryBuilder.append(" WHERE mtrack.mailing_id = ? AND cust.sys_tracking_veto > 0");

		return selectInt(logger, queryBuilder.toString(), mailingId);
	}

	private boolean isTrackingAvailableForMailing(int mailingId, int companyId) {
		String mailTrackTableName = String.format("mailtrack_%d_tbl", companyId);
		String query = String.format("SELECT COUNT(mailing_id) FROM %s WHERE mailing_id = ?", mailTrackTableName);
		return BooleanUtils.toBoolean(selectInt(logger, query, mailingId));
	}

	public Date getScheduledSendTime(int mailingID){
		String sql = "SELECT MAX(senddate) FROM maildrop_status_tbl WHERE mailing_id = ?";
		return select(logger, sql, Date.class, mailingID);
	}

	/**
	 * return an array with two ints [noOfSoftbounces,noOfHardbounces]
	 * @return int [noOfSoftbounces,noOfHardbounces]
	 */
	public int[] getBounces(Integer mailingId, Integer companyId, String language, String selectedTargets) throws Exception {
		int soft = 0;
		int hard = 0;
		List<BouncesRow> list = new MailingBouncesDataSet().getBouncesWithDetail(companyId, mailingId, language, selectedTargets, MailingBouncesDataSet.BounceType.BOTH, null);
		
		for (BouncesRow row : list) {
			if (row.getDetail()<510) {
				soft += row.getCount();
			} else {
				hard += row.getCount();
			}
		}
		return new int[]{soft,hard};
	}

    private List<Integer> parseMailingIds(String mailings) {
        try {
        	List<Integer> mailingIds = new LinkedList<>();
            if (!StringUtils.isEmpty(mailings)) {
                for (String mailingId : mailings.split(",")) {
                    mailingIds.add(Integer.valueOf(mailingId.trim()));
                }
            }
            return mailingIds;
        } catch (Exception e) {
            logger.error(MessageFormat.format("Error occured: {0}", e.getMessage()), e);
            return new LinkedList<>();
        }
    }
}
