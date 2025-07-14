/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password.policy;

/*
 * Note:
 * 
 * When adding or removing policies from this enum,
 * don't forget to updated field.js!
 */
public enum PasswordPolicies {
	LENGTH_8_MIXED(new Length8MixedPasswordPolicy()),
	LENGTH_12_SIMPLE(new Length12SimplePasswordPolicy());
	
	/** Default password policy. */
	public static final PasswordPolicies DEFAULT_POLICY = LENGTH_8_MIXED;
	
	private PasswordPolicy passwordPolicy;
	
	PasswordPolicies(final PasswordPolicy policy) {
		this.passwordPolicy = policy;
	}
	
	public final PasswordPolicy getPasswordPolicy() {
		return this.passwordPolicy;
	}
	
	public final String getPolicyName() {
		return this.name();
	}

	/**
	 * Returns the password policy by given name.
	 * If the password policy name is unknown, the default policy ({@link #DEFAULT_POLICY}) is returned.
	 * 
	 * @param name name of policy
	 * 
	 * @return policy for name or default policy
	 */
	public static final PasswordPolicies findByName(final String name) {
		for(final PasswordPolicies pp : values()) {
			if(pp.name().equals(name)) {
				return pp;
			}
		}
			
		return DEFAULT_POLICY;
	}
}
