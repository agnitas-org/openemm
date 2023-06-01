package com.agnitas.emm.core.mailing.service;

import com.agnitas.beans.Admin;

public interface MailingPriorityService {
    int getMailingPriority(int companyId, int mailingId);
    boolean setPrioritizationAllowed(Admin admin, int mailingId, boolean isAllowed);
}
