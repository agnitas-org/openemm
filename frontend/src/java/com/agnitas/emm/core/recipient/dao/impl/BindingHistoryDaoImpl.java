/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.dao.impl;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.log4j.Logger;

import com.agnitas.emm.core.recipient.dao.BindingHistoryDao;

public class BindingHistoryDaoImpl extends BaseDaoImpl implements BindingHistoryDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(BindingHistoryDaoImpl.class);

	@Override
	public void recreateBindingHistoryTrigger(int companyID) {
		if (isOracleDB()) {
			String sql = "CREATE OR REPLACE TRIGGER hst_customer_" + companyID + "_bind_trigger\n"
				+ "BEFORE DELETE OR UPDATE ON customer_" + companyID + "_binding_tbl\n"
				+ "FOR EACH ROW\n"
				+ "DECLARE\n"
					+ "\tv_error_msg VARCHAR2(255);\n"
					+ "\tv_hist	 VARCHAR2(200);\n"
					+ "\tv_email VARCHAR2(100);\n"
					+ "\tv_cust NUMBER;\n"
				+ "BEGIN\n"
					+ "\tv_hist := 'Client: ' || sys_context('USERENV','HOST') || '|' || sys_context('USERENV','OS_USER');\n"
					+ "\tv_cust := :old.customer_id;\n"
					+ "\tEXECUTE IMMEDIATE 'SELECT email FROM customer_" + companyID + "_tbl WHERE customer_id = ' || v_cust into v_email;\n"
					+ "\tIF DELETING THEN\n"
						+ "\t\tINSERT INTO hst_customer_" + companyID + "_binding_tbl\n"
							+ "\t\t\t(customer_id, email, mailinglist_id, user_type, user_status, user_remark, referrer, creation_date, timestamp, exit_mailing_id, mediatype, timestamp_change, change_type, client_info)\n"
						+ "\t\tVALUES\n"
							+ "\t\t\t(:old.customer_id, v_email, :old.mailinglist_id, :old.user_type, :old.user_status, :old.user_remark, :old.referrer, :old.creation_date, :old.timestamp, :old.exit_mailing_id, :old.mediatype, CURRENT_TIMESTAMP, 0, v_hist);\n"
					+ "\tELSIF UPDATING THEN\n"
						+ "\t\tIF ( (:old.USER_TYPE != :new.user_type) OR (:old.user_status != :new.user_status) OR (:old.mediatype != :new.mediatype)) THEN\n"
							+ "\t\t\tINSERT INTO hst_customer_" + companyID + "_binding_tbl\n"
								+ "\t\t\t\t(customer_id, email, mailinglist_id, user_type, user_status, user_remark, referrer, creation_date, timestamp, exit_mailing_id, mediatype, timestamp_change, change_type, client_info)\n"
							+ "\t\t\tVALUES\n"
								+ "\t\t\t\t(:old.customer_id, v_email, :old.mailinglist_id, :old.user_type, :old.user_status, :old.user_remark, :old.referrer, :old.creation_date, :old.timestamp, :old.exit_mailing_id, :old.mediatype, CURRENT_TIMESTAMP, 1, v_hist);\n"
						+ "\t\tEND IF; "
					+ "\tEND IF;\n"
				+ "EXCEPTION WHEN OTHERS THEN\n"
					+ "\tv_error_msg := SQLERRM;\n"
					+ "\temm_log_db_errors(v_error_msg, " + companyID + ", 'hst_customer_" + companyID + "_bind_trg');\n"
				+ "END;";
			execute(logger, sql);
		} else {
			execute(logger, "DROP TRIGGER IF EXISTS hst_customer_" + companyID + "_bind_delete_trigger");
			execute(logger, "CREATE TRIGGER hst_customer_" + companyID + "_bind_delete_trigger\n" +
				"BEFORE DELETE ON customer_" + companyID + "_binding_tbl FOR EACH ROW\n" +
				"  BEGIN\n" +
				"    DECLARE v_error_msg VARCHAR(255);\n" +
				"    DECLARE v_hist      VARCHAR(200);\n" +
				"    DECLARE v_email     VARCHAR(100);\n" +
				"    DECLARE v_cust      INT;\n" +
				"\n" +
				"    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION\n" +
				"    BEGIN\n" +
				"      GET DIAGNOSTICS CONDITION 1 @msg = MESSAGE_TEXT;\n" +
				"      CALL emm_log_db_errors(@msg, " + companyID + ", 'hst_customer_" + companyID + "_bind_delete_trigger');\n" +
				"    END;\n" +
				"\n" +
				"    SELECT CONCAT('Client: ', USER()) INTO v_hist;\n" +
				"    SELECT OLD.customer_id INTO v_cust;\n" +
				"    SELECT email FROM customer_" + companyID + "_tbl WHERE customer_id = v_cust INTO v_email;\n" +
				"\n" +
				"    INSERT INTO hst_customer_" + companyID + "_binding_tbl (\n" +
				"      customer_id,\n" +
				"      email,\n" +
				"      mailinglist_id,\n" +
				"      user_type,\n" +
				"      user_status,\n" +
				"      user_remark,\n" +
				"      referrer,\n" +
				"      creation_date,\n" +
				"      timestamp,\n" +
				"      exit_mailing_id,\n" +
				"      mediatype,\n" +
				"      timestamp_change,\n" +
				"      change_type,\n" +
				"      client_info\n" +
				"    ) VALUES (\n" +
				"      OLD.customer_id,\n" +
				"      v_email,\n" +
				"      OLD.mailinglist_id,\n" +
				"      OLD.user_type,\n" +
				"      OLD.user_status,\n" +
				"      OLD.user_remark,\n" +
				"      OLD.referrer,\n" +
				"      OLD.creation_date,\n" +
				"      OLD.timestamp,\n" +
				"      OLD.exit_mailing_id,\n" +
				"      OLD.mediatype,\n" +
				"      CURRENT_TIMESTAMP,\n" +
				"      0,\n" +
				"      v_hist);\n" +
				"  END");
			
			execute(logger, "DROP TRIGGER IF EXISTS hst_customer_" + companyID + "_bind_update_trigger");
			execute(logger, "CREATE TRIGGER hst_customer_" + companyID + "_bind_update_trigger\n" +
				"BEFORE UPDATE ON customer_" + companyID + "_binding_tbl FOR EACH ROW\n" +
				"  BEGIN\n" +
				"    DECLARE v_error_msg VARCHAR(255);\n" +
				"    DECLARE v_hist      VARCHAR(200);\n" +
				"    DECLARE v_email     VARCHAR(100);\n" +
				"    DECLARE v_cust      INT;\n" +
				"\n" +
				"    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION\n" +
				"    BEGIN\n" +
				"      GET DIAGNOSTICS CONDITION 1 @msg = MESSAGE_TEXT;\n" +
				"      CALL emm_log_db_errors(@msg, " + companyID + ", 'hst_customer_" + companyID + "_bind_update_trigger');\n" +
				"    END;\n" +
				"\n" +
				"    SELECT CONCAT('Client: ', USER()) INTO v_hist;\n" +
				"    SELECT OLD.customer_id INTO v_cust;\n" +
				"    SELECT email FROM customer_" + companyID + "_tbl WHERE customer_id = v_cust INTO v_email;\n" +
				"\n" +
				"    IF ((OLD.user_type != NEW.user_type) OR (OLD.user_status != NEW.user_status) OR (OLD.mediatype != NEW.mediatype)) THEN\n" +
				"      INSERT INTO hst_customer_" + companyID + "_binding_tbl (\n" +
				"        customer_id,\n" +
				"        email,\n" +
				"        mailinglist_id,\n" +
				"        user_type,\n" +
				"        user_status,\n" +
				"        user_remark,\n" +
				"        referrer,\n" +
				"        creation_date,\n" +
				"        timestamp,\n" +
				"        exit_mailing_id,\n" +
				"        mediatype,\n" +
				"        timestamp_change,\n" +
				"        change_type,\n" +
				"        client_info\n" +
				"      ) VALUES (\n" +
				"        OLD.customer_id,\n" +
				"        v_email,\n" +
				"        OLD.mailinglist_id,\n" +
				"        OLD.user_type,\n" +
				"        OLD.user_status,\n" +
				"        OLD.user_remark,\n" +
				"        OLD.referrer,\n" +
				"        OLD.creation_date,\n" +
				"        OLD.timestamp,\n" +
				"        OLD.exit_mailing_id,\n" +
				"        OLD.mediatype,\n" +
				"        CURRENT_TIMESTAMP,\n" +
				"        1,\n" +
				"        v_hist);\n" +
				"    END IF;\n" +
				"  END");
		}
	}
}
