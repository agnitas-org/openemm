package com.agnitas.emm.core.mailing.service;

import com.agnitas.beans.ComAdmin;

public interface MailingPriorityService {
    int getMailingPriority(int companyId, int mailingId);
    boolean setPrioritizationAllowed(ComAdmin admin, int mailingId, boolean isAllowed);
}
