/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.beans.ComRecommendationStatistic;
import com.agnitas.dao.ComRecommendationStatisticDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;

/**
 * DAO handler for RecommendationStatistic-Objects
 * This class is compatible with oracle and mysql datasources and databases
 */
public class ComRecommendationStatisticDaoImpl extends BaseDaoImpl implements ComRecommendationStatisticDao {
	private static final transient Logger logger = Logger.getLogger(ComRecommendationStatisticDaoImpl.class);
	
	private static final String TABLE = "recommendation_stat_tbl";
	
	private static final String FIELD_RECOMMENDATION_ID = "recommendation_id";
	private static final String FIELD_CREATION_DATE = "creation_date"; // default: sysdate
	private static final String FIELD_SENDER_CUSTOMER_ID = "sender_customer_id";
	private static final String FIELD_SENDER_EMAIL = "sender_email";
	private static final String FIELD_RECEIVER_CUSTOMER_ID = "receiver_customer_id";
	private static final String FIELD_RECEIVER_EMAIL = "receiver_email";
	private static final String FIELD_ISNEW = "isnew";
	private static final String FIELD_STATUS = "status"; // 0 = recommendation, 1 = participation
	
	private static final String[] FIELD_NAMES = new String[]{FIELD_RECOMMENDATION_ID, FIELD_CREATION_DATE, FIELD_SENDER_CUSTOMER_ID, FIELD_SENDER_EMAIL, FIELD_RECEIVER_CUSTOMER_ID, FIELD_RECEIVER_EMAIL, FIELD_ISNEW, FIELD_STATUS};
	
	private static final String INSERT = "INSERT INTO " + TABLE + " (" + StringUtils.join(FIELD_NAMES, ", ") + ")" + " VALUES (" + AgnUtils.repeatString("?", FIELD_NAMES.length, ", ") + ")";

	@Override
	@DaoUpdateReturnValueCheck
	public void insert(ComRecommendationStatistic comRecommendationStatistic) {
		int insertedLines = update(logger, INSERT,
			comRecommendationStatistic.getRecommendationId(),
			comRecommendationStatistic.getCreationDate(),
			comRecommendationStatistic.getSenderCustomerId(),
			comRecommendationStatistic.getSenderEmail(),
			comRecommendationStatistic.getReceiverCustomerId(),
			comRecommendationStatistic.getReceiverEmail(),
			comRecommendationStatistic.getIsNew(),
			comRecommendationStatistic.getStatus().getDbValue());
		
		if (insertedLines != 1) {
			throw new RuntimeException("Illegal insert result");
		}
	}
}
