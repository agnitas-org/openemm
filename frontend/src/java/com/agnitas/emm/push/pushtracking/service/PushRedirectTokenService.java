package com.agnitas.emm.push.pushtracking.service;

import com.agnitas.emm.push.pushtracking.RedirectTokenException;

public interface PushRedirectTokenService {
	public String registerRedirectData(final Object arg0, final int arg1, final int arg2) throws RedirectTokenException;
}
