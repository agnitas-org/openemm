/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.agnitas.beans.ProfileField;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.DbUtilities;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.ComRecipientHistory;
import com.agnitas.beans.impl.ComRecipientHistoryImpl;
import com.agnitas.emm.core.recipient.CannotUseViewsException;
import com.agnitas.emm.core.recipient.RecipientProfileHistoryException;
import com.agnitas.emm.core.recipient.dao.RecipientProfileHistoryDao;

/**
 * Implementation of {@link RecipientProfileHistoryDao} interface.
 */
public class RecipientProfileHistoryBasicDaoImpl extends BaseDaoImpl implements RecipientProfileHistoryDao {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(RecipientProfileHistoryBasicDaoImpl.class);
	
	
	/**
	 * Implementation of {@link RowMapper} for profile field history.
	 */
	private static class HistoryRowMapper implements RowMapper<ComRecipientHistory> {
		@Override
		public ComRecipientHistory mapRow(ResultSet resultSet, int row) throws SQLException {
			ComRecipientHistoryImpl history = new ComRecipientHistoryImpl();

			history.setChangeDate(resultSet.getTimestamp("change_date"));
			history.setFieldName(resultSet.getString("name"));
			history.setOldValue(resultSet.getString("old_value"));
			history.setNewValue(resultSet.getString("new_value"));

			return history;
		}
	}

	/**
	 * Enum of trigger events used internally for building SQL code for triggers.
	 */
	protected enum TriggerEvent {
		/** <i>ON INSERT</i> trigger event. */
		INSERT("INSERT", "INS", false, true, HistoryUpdateType.INSERT),

		/** <i>ON DELETE</i> trigger event. */
		DELETE("DELETE", "DEL", true, false, HistoryUpdateType.DELETE),

		/** <i>ON UPDATE</i> trigger event. */
		UPDATE("UPDATE", "UPD", true, true, HistoryUpdateType.UPDATE);

		/** SQL-side name of trigger event. */
		public final String sqlEventName;

		/** Short code of trigger event, used in trigger name. */
		public final String eventShort;

		/** Flag, if "old value is to be mapped to history table. */
		public final boolean mapOld;

		/** Flag, if new value is to be mapped to history table. */
		public final boolean mapNew;

		/** Indicator for type of profile field update. */
		public final HistoryUpdateType updateType;

		/**
		 * Create new enum item.
		 * 
		 * @param sqlEventName SQL-side name of trigger event
		 * @param eventShort short code of trigger event
		 * @param mapOld map old value to history if <code>true</code>
		 * @param mapNew map new value to history table if <code>true</code>
		 * @param updateType type of update
		 */
		TriggerEvent(final String sqlEventName, final String eventShort, final boolean mapOld, final boolean mapNew,
				final HistoryUpdateType updateType) {
			this.sqlEventName = sqlEventName;
			this.eventShort = eventShort;
			this.mapOld = mapOld;
			this.mapNew = mapNew;
			this.updateType = updateType;
		}
	}

	@Override
	public void afterProfileFieldStructureModification(final int companyID, final List<ProfileField> profileFields) throws RecipientProfileHistoryException {
		createOrReplaceTrigger(TriggerEvent.INSERT, companyID, profileFields);
		createOrReplaceTrigger(TriggerEvent.UPDATE, companyID, profileFields);
		createOrReplaceTrigger(TriggerEvent.DELETE, companyID, profileFields);
	}

	@Override
	public List<ComRecipientHistory> listProfileFieldHistory(final int recipientID, @VelocityCheck final int companyID) {
		String recipientHistoryTable = buildHistoryTableName(companyID);
		boolean isRecipientHistoryTableExist = DbUtilities.checkIfTableExists(getDataSource(), recipientHistoryTable);

		if (isRecipientHistoryTableExist) {
			final String sql = "SELECT *" +
					" FROM " + recipientHistoryTable +
					" WHERE customer_id = ?" +
					" ORDER BY change_date ASC";

			return select(logger, sql, new HistoryRowMapper(), recipientID);
		}

		return Collections.emptyList();
	}

	/**
	 * Processes a single trigger.
	 * 
	 * First, the (possibly) existing trigger is attempted to be dropped. Then the
	 * triggers in created.
	 *
	 * @param triggerEvent trigger event
	 * @param companyID company ID
	 * @param profileFields list of profile fields to be included in history
	 */
	protected final void createOrReplaceTrigger(final TriggerEvent triggerEvent, final int companyID, List<ProfileField> profileFields) {
		try {
			if (isOracleDB()) {
				execute(logger, "DROP TRIGGER " + buildTriggerName(companyID, triggerEvent));
			} else {
				execute(logger, "DROP TRIGGER IF EXISTS " + buildTriggerName(companyID, triggerEvent));
			}
		} catch (Exception e) {
			// Reduced level to INFO, because dropping trigger also fails, if trigger does
			// not exist
			if (logger.isInfoEnabled()) {
				logger.info(String.format("Couldn't delete history trigger (%s) for company %d", triggerEvent, companyID), e);
			}
		}

		execute(logger, createTriggerStatement(companyID, triggerEvent, profileFields));
	}

	/**
	 * Creates the SQL statement for a specific trigger.
	 * 
	 * @param triggerEvent trigger event
	 * @param companyID company ID
	 * @param profileFields list of profile fields to be included in history
	 * 
	 * @return SQL statement to create a specific trigger
	 */
	public String createTriggerStatement(final int companyID, final TriggerEvent triggerEvent, final List<ProfileField> profileFields) {
		String triggerReferenceOld;
		String triggerReferenceNew;
		if (isOracleDB()) {
			triggerReferenceOld = ":old.";
			triggerReferenceNew = ":new.";
		} else {
			triggerReferenceOld = "OLD.";
			triggerReferenceNew = "NEW.";
		}
		
		StringBuilder buffer = new StringBuilder("CREATE TRIGGER " + buildTriggerName(companyID, triggerEvent) + "\n");
		buffer.append("AFTER " + triggerEvent.sqlEventName + " ON " + buildSourceTableName(companyID) + " FOR EACH ROW" + (isOracleDB() ? " ENABLE" : "") + "\n");
		buffer.append("  BEGIN\n");
		
		if(!isOracleDB()) {			
			buffer.append("    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION\n");
			buffer.append("    BEGIN\n");
			buffer.append("      GET DIAGNOSTICS CONDITION 1 @msg = MESSAGE_TEXT;\n");
			buffer.append(String.format("      CALL emm_log_db_errors(@msg, %d, '%s');\n", companyID, buildTriggerName(companyID, triggerEvent)));
			buffer.append("    END;\n");
		}
		
		// Create INSERT statement for each profile field column
		for (ProfileField profileField : profileFields) {
			final StringBuffer insertBuffer = new StringBuffer();

			insertBuffer.append("  INSERT INTO ").append(buildHistoryTableName(companyID))
				.append(" (customer_id, change_type, name, old_value, new_value) VALUES (");

			// Append customer ID (according to PL/SQL convention, OLD and NEW are only
			// available for specific trigger events. So we have to decide, which one to use here
			if (triggerEvent.mapNew) {
				insertBuffer.append(triggerReferenceNew + "customer_id");
			} else {
				insertBuffer.append(triggerReferenceOld + "customer_id");
			}
			insertBuffer.append(", ");

			// Append type of modification
			insertBuffer.append(triggerEvent.updateType.typeCode).append(", ");

			// Append modified profile field
			insertBuffer.append("LOWER('").append(profileField.getColumn()).append("'), ");

			// Append value before modification
			if (triggerEvent.mapOld) {
				insertBuffer.append(typeConvert(profileField, triggerReferenceOld + profileField.getColumn()));
			} else {
				insertBuffer.append("NULL");
			}
			insertBuffer.append(", ");

			// Append value after modification
			if (triggerEvent.mapNew) {
				insertBuffer.append(typeConvert(profileField, triggerReferenceNew + profileField.getColumn()));
			} else {
				insertBuffer.append("NULL");
			}

			insertBuffer.append(");");

			if (triggerEvent == TriggerEvent.UPDATE) {
				String columnName = profileField.getColumn();

				buffer.append("  IF ").append("(")
					.append(triggerReferenceOld + columnName).append(" <> ").append(triggerReferenceNew + columnName).append(")")
					.append(" OR (")
					.append(triggerReferenceOld + columnName).append(" IS NULL AND ").append(triggerReferenceNew + columnName).append(" IS NOT NULL)")
					.append(" OR (")
					.append(triggerReferenceOld + columnName).append(" IS NOT NULL AND ").append(triggerReferenceNew + columnName).append(" IS NULL)")

					.append("  THEN\n");
				buffer.append("    ").append(insertBuffer.toString()).append("\n");
				buffer.append("  END IF;\n");
			} else {
				buffer.append("  ").append(insertBuffer.toString()).append("\n");
			}
		}
		
		if(isOracleDB()) {
			buffer.append(" EXCEPTION WHEN OTHERS THEN\n");
			buffer.append(String.format("    emm_log_db_errors(SQLERRM, %d, '%s');\n", companyID, buildTriggerName(companyID, triggerEvent)));
		}
		
		buffer.append("  END;");
		
		return buffer.toString();
	}

	/**
	 * Checks type of profile field and embeds code for profile field in type
	 * conversion code, if necessary.
	 * 
	 * @param profileField profile field
	 * @param victim SQL fragment, that may be embedded inside some type conversion code
	 * 
	 * @return SQL fragment for reading profile field value
	 */
	private String typeConvert(final ProfileField profileField, final String victim) {
		SimpleDataType dataType = DbColumnType.getSimpleDataType(profileField.getDataType());

		if (dataType == SimpleDataType.Date) {
			if (isOracleDB()) {
				return String.format("to_char(%s, 'yyyy-mm-dd HH24:mi:ss')", victim);
			} else {
				return String.format("date_format(%s, '%%Y-%%m-%%d %%H:%%i:%%s')", victim);
			}
		} else {
			return victim;
		}
	}

	/**
	 * Builds name of customer table based on company ID.
	 * 
	 * @param companyID company ID
	 * 
	 * @return name of customer table
	 */
	public static String buildSourceTableName(final int companyID) {
		return String.format("customer_%d_tbl", companyID);
	}

	/**
	 * Builds name of history table based on company ID.
	 * 
	 * @param companyID company ID
	 * 
	 * @return name of history table
	 */
	public static String buildHistoryTableName(final int companyID) {
		return String.format("hst_customer_%d_tbl", companyID);
	}

	/**
	 * Builds the name of the trigger according to given data.
	 * 
	 * @param companyID company ID
	 * @param event trigger event
	 * 
	 * @return name of trigger
	 */
	public static String buildTriggerName(final int companyID, final TriggerEvent event) {
		return String.format("hst_customer_%d_%s_trigger", companyID, event.eventShort);
	}

	protected final void checkCompanyTables(final int companyID) throws RecipientProfileHistoryException {
		final String customerTable = String.format("customer_%d_tbl", companyID);
		
		if (DbUtilities.checkTableIsView(customerTable, getDataSource())) {
			throw new CannotUseViewsException(companyID);
		}
	}
}
