/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.util.Map;

public interface Mailgun {
    /**
     * Initialize internal data
     * @param status_id the string version of the statusID to use
     */
    void initialize (String status_id) throws Exception;

    /**
     * Setup a mailout without starting generation
     *
     * @param opts options to control the setup beyond DB information
     */
    void prepare (Map <String, Object> opts) throws Exception;

    /**
     * Execute an already setup mailout
     *
     * @param opts options to control the execution beyond DB information
     */
    void execute (Map <String, Object> opts) throws Exception;

    /**
     * Full execution of a mail generation
     *
     * @param custid optional customer id
     * @return Status string
     */
    String fire(String custid) throws Exception;

    /** Cleaup mailout
     */
    void done () throws Exception;
}
