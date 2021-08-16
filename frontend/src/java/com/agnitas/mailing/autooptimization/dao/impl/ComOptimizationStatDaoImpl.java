/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.dao.impl;

import java.util.Map;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;

import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.mailing.autooptimization.dao.ComOptimizationStatDao;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;

public class ComOptimizationStatDaoImpl extends BaseDaoImpl implements ComOptimizationStatDao {
	private static final transient Logger logger = Logger.getLogger(ComOptimizationStatDaoImpl.class);
	
	@Override
	public int getBounces(int mailingID, @VelocityCheck int companyID) {
		String query = "SELECT COUNT(DISTINCT customer_id) FROM customer_" + companyID + "_binding_tbl"
			+ " WHERE user_status = ? AND exit_mailing_id = ?"
			+ " AND user_type NOT IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "')";
		return selectInt(logger, query, UserStatus.Bounce.getStatusCode(), mailingID);
	}

    private String getRecipientsTypeCondition(String recipientsType) {
        StringBuilder queryBuilder = new StringBuilder();
        String user_type;
        if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
        	user_type = "'" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "'";
        } else if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
        	user_type = "'" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "'";
        } else {
        	user_type = "";
        }
        if (!"".equals(user_type)) {
            queryBuilder
                    .append(" and exists (select 1 from customer_<COMPANYID>_binding_tbl b, mailing_tbl m ")
                    .append("              where b.user_type in (").append(user_type).append(") ")
                    .append("                and m.mailing_id = x.mailing_id ")
                    .append("                and b.mailinglist_id = m.mailinglist_id ")
                    .append("                and b.customer_id = x.customer_id")
                    .append("     )");
        }
        return queryBuilder.toString();
    }

    @Override
    public int getClicks(int mailingId, @VelocityCheck int companyId, String recipientsType) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("select count(distinct x.customer_id) ")
                .append("  from rdirlog_<COMPANYID>_tbl x ")
                .append("where x.mailing_id = ? ")
                .append(getRecipientsTypeCondition(recipientsType));
        return select(logger, queryBuilder.toString().replace("<COMPANYID>", String.valueOf(companyId)), Integer.class, mailingId);
    }

    @Override
    public int getOpened(int mailingId, @VelocityCheck int companyId, String recipientsType) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("select count(distinct x.customer_id) ")
                .append("  from onepixellog_device_<COMPANYID>_tbl x ")
                .append(" where x.mailing_id = ? ")
                .append(getRecipientsTypeCondition(recipientsType));
        return select(logger, queryBuilder.toString().replace("<COMPANYID>", String.valueOf(companyId)), Integer.class, mailingId);
    }

    @Override
	public int getOptOuts(int mailingID,@VelocityCheck int companyID) {
		String query = "SELECT count(distinct (bind.customer_id)) AS optout FROM " +
			" customer_<COMPANYID>_binding_tbl bind " +
			" WHERE bind.exit_mailing_id= ? AND (bind.user_status = 3 OR  bind.user_status = 4)" +
			" AND user_type NOT IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') and mailinglist_id=(" +
			"select mailinglist_id from mailing_tbl where mailing_id = ?) ";

		query = query.replace("<COMPANYID>", Integer.toString(companyID));
			
		Map<String, Object> optoutMap = select(logger, query, mailingID, mailingID).get(0);
		return optoutMap.get("optout") != null ? ((Number)optoutMap.get("optout")).intValue():0 ;
	}

    @Override
    public int getSend(int mailingId, String recipientsType) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("SELECT ").append("COALESCE(SUM(x.no_of_mailings), 0) ")
                .append("  FROM mailing_account_tbl x, mailing_tbl m ")
                .append(" WHERE x.mailing_id = ? ")
                .append("   AND x.mailing_id = m.mailing_id ")
                .append("   AND ((m.mailing_type = ? and x.status_field = 'E') OR (m.mailing_type <> ?)) ");
        if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder
                .append("   AND (' x.status_field = 'A' OR x.status_field = 'T')");
        } else if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder
                .append("   AND ((m.mailing_type = ").append(MailingTypes.DATE_BASED.getCode()).append(" AND x.status_field = 'R') OR (x.status_field = 'W')) ");
        }
        return select(logger, queryBuilder.toString(), Integer.class, mailingId, MailingTypes.ACTION_BASED.getCode(), MailingTypes.ACTION_BASED.getCode());
    }

    @Override
	public double getRevenue(int mailingID, @VelocityCheck int companyID) {
        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder
                    .append("SELECT COALESCE(SUM(num_parameter), 0) ")
                    .append("  FROM rdirlog_").append(companyID).append("_val_num_tbl ")
                    .append(" WHERE mailing_id = ? ")
                    .append("   AND page_tag = ?");
            return select(logger, queryBuilder.toString(), Double.class, mailingID, "revenue");
        }
        catch (Exception e) {
            logger.warn("Error while getting the revenue", e);
            return 0;
        }
	}
}
