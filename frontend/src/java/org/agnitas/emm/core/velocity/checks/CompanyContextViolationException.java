/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity.checks;

/**
 * Exception indicating a violation of the company context.
 */
@Deprecated // After completion of EMM-8360, this class can be removed without replacement
public class CompanyContextViolationException extends VelocityCheckerException {

	/** Serial version UID. */
	private static final long serialVersionUID = 5211377912170861899L;

	/** Company ID that is violating the context. */
	private final int invalidCompanyId;

	/** ID of company that is executing the script. */
	private final int contextCompanyId;

	/**
	 * Instantiates a new company context violation exception.
	 *
	 * @param invalidCompanyId
	 *            the invalid company id
	 * @param contextCompanyId
	 *            the context company id
	 */
	public CompanyContextViolationException(int invalidCompanyId, int contextCompanyId) {
		super("Script violates context of company " + contextCompanyId + " (invalid company ID is " + invalidCompanyId + ")");

		this.invalidCompanyId = invalidCompanyId;
		this.contextCompanyId = contextCompanyId;
	}

	/**
	 * Gets the context company id.
	 *
	 * @return the context company id
	 */
	public int getContextCompanyId() {
		return this.contextCompanyId;
	}

	/**
	 * Gets the invalid company id.
	 *
	 * @return the invalid company id
	 */
	public int getInvalidCompanyId() {
		return this.invalidCompanyId;
	}
}
