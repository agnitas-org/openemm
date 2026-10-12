/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

DO
$$
	DECLARE
		company_row RECORD;
	BEGIN
		FOR company_row IN SELECT company_id FROM company_tbl WHERE status IN ('active', 'locked') ORDER BY company_id
		LOOP
			IF to_regclass('cust' || company_row.company_id || 'b$exmid_ustat_uty$idx') IS NULL THEN
				BEGIN
					EXECUTE 'CREATE INDEX cust' || company_row.company_id || 'b$exmid_ustat_uty$idx ON customer_' || company_row.company_id || '_binding_tbl (exit_mailing_id, user_status, user_type) TABLESPACE data_cust_index';
				EXCEPTION WHEN OTHERS THEN
					RAISE NOTICE 'Error adding index to customer_%_binding_tbl for GWUA-7126', company_row.company_id;
				END;
			END IF;
		END LOOP;
	END;
$$;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.07.362', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
