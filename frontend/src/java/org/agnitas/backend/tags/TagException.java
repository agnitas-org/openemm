package org.agnitas.backend.tags;

import org.agnitas.backend.EMMTagException;

public class TagException extends EMMTagException {
	private static final long serialVersionUID = -1095894780058635365L;

	public TagException (Tag tag, String msg) {
		super (null, null, msg);
	}
}
