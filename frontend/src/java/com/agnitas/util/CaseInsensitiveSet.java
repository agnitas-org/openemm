/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.util;

import java.util.Collection;
import java.util.HashSet;

/**
 * String Set that ignores the String case
 */
public class CaseInsensitiveSet extends HashSet<String> {
	private static final long serialVersionUID = -7971851798927626414L;

	public CaseInsensitiveSet() {
	}
	
	public CaseInsensitiveSet(Collection<? extends String> collection) {
		addAll(collection);
	}
	
	public CaseInsensitiveSet(String[] values) {
		for (String value : values) {
			add(value.toLowerCase());
		}
	}

	@Override
	public boolean contains(Object object) {
		if (object == null) {
			return super.contains(null);
		} else {
			return super.contains(object.toString().toLowerCase());
		}
	}

	@Override
	public boolean add(String value) {
		if (value == null) {
			return super.add(null);
		} else {
			return super.add(value.toLowerCase());
		}
	}

	@Override
	public boolean remove(Object object) {
		if (object == null) {
			return super.remove(null);
		} else {
			return super.remove(object.toString().toLowerCase());
		}
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		if (collection == null) {
			return false;
		} else {
			for (Object object : collection) {
				if (object == null) {
					if (!super.contains(null)) {
						return false;
					}
				} else {
					if (!super.contains(object.toString().toLowerCase())) {
						return false;
					}
				}
			}
			return true;
		}
	}

	@Override
	public boolean addAll(Collection<? extends String> collection) {
		if (collection == null) {
			return false;
		} else {
			boolean returnValue = true;
			for (String value : collection) {
				if (value == null) {
					if (!super.add(null)) {
						returnValue = false;
					}
				} else {
					if (!super.add(value.toLowerCase())) {
						returnValue = false;
					}
				}
			}
			return returnValue;
		}
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		CaseInsensitiveSet removeSet = new CaseInsensitiveSet();
		for (String value : this) {
			boolean contained = false;
			for (Object object : collection) {
				if (value == null && object == null) {
					contained = true;
					break;
				} else if (value != null && value.equals(object.toString().toLowerCase())) {
					contained = true;
					break;
				}
			}
			
			if (!contained) {
				removeSet.add(value);
			}
		}
		return removeAll(removeSet);
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		if (collection == null) {
			return false;
		} else {
			for (Object object : collection) {
				if (object == null) {
					if (!super.remove(null)) {
						return false;
					}
				} else {
					if (!super.remove(object.toString().toLowerCase())) {
						return false;
					}
				}
			}
			return true;
		}
	}
}
