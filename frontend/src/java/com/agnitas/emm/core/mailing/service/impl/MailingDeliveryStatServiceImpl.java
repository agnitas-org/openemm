/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.util.Date;
import java.util.Objects;

import com.agnitas.beans.DeliveryStat;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.impl.MailingBackendLog;
import com.agnitas.dao.DeliveryStatDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.service.MailingDeliveryStatService;
import com.agnitas.emm.core.mailing.service.MailingStopService;
import com.agnitas.post.TriggerdialogService;
import com.agnitas.util.DateUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailingDeliveryStatServiceImpl implements MailingDeliveryStatService {

    private static final Logger logger = LogManager.getLogger(MailingDeliveryStatServiceImpl.class);

    private DeliveryStatDao deliveryStatDao;
    private MailingDao mailingDao;
    private MailingStopService mailingStopService;
    
    /**
     * Optional available service
     */
    private TriggerdialogService triggerdialogService = null;

	@Override
	public int getSentMails(int maildropId) {
		return deliveryStatDao.getSentMails(maildropId);
	}

	@Override
    public DeliveryStat getDeliveryStats(int companyID, int mailingID, MailingType mailingType) {
        if (triggerdialogService != null && triggerdialogService.isPostMailing(mailingDao.getMailing(mailingID, companyID))) {
        	DeliveryStat deliveryStatistic = triggerdialogService.getTriggerdialogDeliveryStatus(mailingID);
			if (deliveryStatistic == null) {
				deliveryStatistic = new DeliveryStat();
			}
			deliveryStatistic.setTotalMails(deliveryStatDao.getTotalMails(mailingID));
			deliveryStatistic.setCancelable(triggerdialogService.canStopMailing(companyID, mailingID));
			deliveryStatistic.setStopped(false);
			deliveryStatistic.setResumable(false);
	        return deliveryStatistic;
    	} else {
            DeliveryStat deliveryStatistic = new DeliveryStat();
            
	        deliveryStatistic.setTotalMails(deliveryStatDao.getTotalMails(mailingID));
	        deliveryStatistic.setCancelable(mailingStopService.canStopMailing(companyID, mailingID));
	        deliveryStatistic.setStopped(mailingStopService.isStopped(companyID, mailingID));

	        if(deliveryStatistic.isStopped()) {
	        	final Date deliveryPauseDate = mailingStopService.getDeliveryPauseDate(companyID, mailingID);
	        	final boolean isResumable;
	        	if(deliveryPauseDate == null) {
	        		isResumable = false;
	        	} else {
	        		isResumable = DateUtilities.addDaysToDate(new Date(), -2).before(deliveryPauseDate);
				}
	        	deliveryStatistic.setResumable(isResumable);
			}
	
	        int statusID = 0;
	        // -------------------------------------- last thing backend did for this mailing:
	        try {
	            MaildropEntry maildropStatus = deliveryStatDao.getLastMaildropStatus(mailingID);
	            
	            
	            if (maildropStatus != null) {
	                if (maildropStatus.getGenStatus() > 0) {
	                    deliveryStatistic.setLastType(String.valueOf(maildropStatus.getStatus()));
	                } else {
	                    deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_SCHEDULED);
	                }
	                statusID = maildropStatus.getId();
	            }
	        } catch (Exception e) {
	            return null;
	        }
	
	        // ----------------------------------------- how many mails in last admin/test backend action?
	        boolean lastBackend;
	        try {
	            MailingBackendLog mailingBackendLog = deliveryStatDao.getLastMailingBackendLog(statusID);
	            if (mailingBackendLog != null) {
	                deliveryStatistic.setLastGenerated(mailingBackendLog.getCurrentMails());
	                deliveryStatistic.setLastTotal(mailingBackendLog.getTotalMails());
	                deliveryStatistic.setLastDate(mailingBackendLog.getTimestamp());
	                deliveryStatistic.setGenerateStartTime(mailingBackendLog.getCreationDate());
	            } else {
	                deliveryStatistic.setLastDate(new Date());
	            }
	            lastBackend = true;
	        } catch (Exception e) {
	            lastBackend = false;
	        }
	
	        // no entry in mailing_backend_log_tbl ==> don't proceed:
	        if (!"NO".equals(deliveryStatistic.getLastType()) && !lastBackend) {
	            return deliveryStatistic;
	        }
	
	        String statusField = getStatusField(mailingType);
	
	        // check generation status first
			MaildropEntry maildropGenerationStatus = deliveryStatDao.getFirstMaildropGenerationStatus(companyID, mailingID, statusField);
			if (maildropGenerationStatus != null) {
				deliveryStatistic.setOptimizeMailGeneration(maildropGenerationStatus.getMailGenerationOptimization());
				deliveryStatistic.setScheduledGenerateTime(maildropGenerationStatus.getGenDate());
				deliveryStatistic.setScheduledSendTime(maildropGenerationStatus.getSendDate());
				final MaildropGenerationStatus aktMdropStatusOrNull = MaildropGenerationStatus.fromCodeOrNull(maildropGenerationStatus.getGenStatus());

				if(aktMdropStatusOrNull != null) {
					switch(aktMdropStatusOrNull) {
					case SCHEDULED:
						deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_SCHEDULED);
						break;
					case NOW:
						deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_SCHEDULED);
						break;
					case WORKING:
						deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_GENERATING);
						break;
					case FINISHED:
						deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_GENERATED);
						break;
					default:
						return deliveryStatistic;

					}
				} else {
					return deliveryStatistic;
				}
			} else {
				// mailing not scheduled for sending:
				deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_NOT_SENT);
			}

	        //----------------------------------------------------------------------
	        if (!deliveryStatistic.getLastType().equalsIgnoreCase(MaildropStatus.WORLD.getCodeString())) {
	            int lastWorldMailingStatusID = mailingDao.getStatusidForWorldMailing(deliveryStatistic.getMailingID(), deliveryStatistic.getCompanyID());
	            if (lastWorldMailingStatusID != 0) {
	                // mailing has been sent, but a test or admin mailing follows
	                MailingBackendLog lastWorldMailingBackendLog = deliveryStatDao.getLastMailingBackendLog(lastWorldMailingStatusID);
	
	                deliveryStatistic.setTotalMails(lastWorldMailingBackendLog.getTotalMails());
	                deliveryStatistic.setGeneratedMails(lastWorldMailingBackendLog.getCurrentMails());
	                deliveryStatistic.setGenerateEndTime(lastWorldMailingBackendLog.getTimestamp());
	                deliveryStatistic.setGenerateStartTime(lastWorldMailingBackendLog.getCreationDate());
	                deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_SENT);
	
	                deliveryStatistic.setSentMails(getSentMails(lastWorldMailingStatusID));
	                deliveryStatistic.setSendStartTime(deliveryStatDao.getSendStartTime(lastWorldMailingStatusID));
	                deliveryStatistic.setSendEndTime(deliveryStatDao.getSendEndTime(lastWorldMailingStatusID));
	            }
	        } else {
	            // detailed stats for mailings beeing generated:
	            try {
	                MailingBackendLog mailingBackendLog = deliveryStatDao.getLastWorldMailingBackendLog(mailingID);
	                if (mailingBackendLog != null) {
	                    deliveryStatistic.setTotalMails(mailingBackendLog.getTotalMails());
	                    deliveryStatistic.setGeneratedMails(mailingBackendLog.getCurrentMails());
	                    deliveryStatistic.setGenerateEndTime(mailingBackendLog.getTimestamp());
	                    deliveryStatistic.setGenerateStartTime(mailingBackendLog.getCreationDate());
	
	                    // loadSendDate
	                    try {
	                        deliveryStatistic.setScheduledSendTime(deliveryStatDao.getSendDateByStatusId(statusID));
	                    } catch (Exception e) {
	                        return deliveryStatistic;
	                    }
	
	                    deliveryStatistic.setSentMails(getSentMails(statusID));
	                    deliveryStatistic.setSendStartTime(deliveryStatDao.getSendStartTime(statusID));
	                    deliveryStatistic.setSendEndTime(deliveryStatDao.getSendEndTime(statusID));
	
	                    if (deliveryStatistic.getGeneratedMails() == deliveryStatistic.getTotalMails()
	                            && deliveryStatistic.getSentMails() == deliveryStatistic.getTotalMails()) {
	                        deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_SENT);
	                    } else if (deliveryStatistic.getGeneratedMails() == deliveryStatistic.getTotalMails()
	                            && deliveryStatistic.getScheduledSendTime().after(new Date())) {
	                        deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_GENERATED);
	                    } else if (deliveryStatistic.getGeneratedMails() == deliveryStatistic.getTotalMails()) {
	                        deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_SENDING);
	                    } else {
	                        deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_GENERATING);
	                    }
	                }
	            } catch (Exception e) {
	                logger.error("Error in getDeliveryStatsForMailingType({}, {}, {}): {}", companyID, mailingID, mailingType, e.getMessage(), e);
	                return deliveryStatistic;
	            }
	        }
	
	        updateCancelStateIfMailingStopped(deliveryStatistic, mailingID);
	        
	        return deliveryStatistic;
        }
    }

    private void updateCancelStateIfMailingStopped(final DeliveryStat deliveryStatistic, final int mailingID) {
        if(mailingStopService.isStopped(mailingID)) {
        	switch(deliveryStatistic.getDeliveryStatus()) {
        	case DeliveryStat.STATUS_GENERATING:
        		deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_PAUSED_GENERATION);
        		break;
        		
        	case DeliveryStat.STATUS_GENERATED: // Fall-through
        	case DeliveryStat.STATUS_SENDING:
            	deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_PAUSED_DELIVERY);
            	break;
            	
        	case DeliveryStat.STATUS_CANCELLED: // Fall-through
        	case DeliveryStat.STATUS_PAUSED_DELIVERY: // Fall-through
        	case DeliveryStat.STATUS_PAUSED_GENERATION:
        		// Do nothing
        		break;
        		
        	case DeliveryStat.STATUS_NOT_SENT:
        		deliveryStatistic.setDeliveryStatus(DeliveryStat.STATUS_CANCELLED);
        		break;
        		
        	case DeliveryStat.STATUS_SENT:
        		// Do nothing
        		break;
        		
        	default:
        		logger.warn("Don't know how to handle delivery status {} of mailing {}", deliveryStatistic.getDeliveryStatus(), mailingID);
        	}
        }
    	
    }
    
    private String getStatusField(MailingType mailingType) {
        if (mailingType != null) {
            switch (mailingType) {
                case NORMAL:
                    return MaildropStatus.WORLD.getCodeString();
        
                case DATE_BASED:
                    return MaildropStatus.DATE_BASED.getCodeString();
        
                case ACTION_BASED:
                    //TODO: check if it's possible to change UNKNOWN_C to ACTION_BASED('E')
                    return MaildropStatus.UNKNOWN_C.getCodeString();
        
                default:
                    //nothing do
            }
        }
        return MaildropStatus.WORLD.getCodeString();
    }
 
    public void setDeliveryStatDao(DeliveryStatDao deliveryStatDao) {
        this.deliveryStatDao = deliveryStatDao;
    }

    public void setMailingDao(MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }
    
    public final void setMailingStopService(final MailingStopService service) {
    	this.mailingStopService = Objects.requireNonNull(service, "Mailing stop service is null");
    }
    
    /**
     * Optional available service
     * @param triggerdialogService
     */
    public final void setTriggerdialogService(final TriggerdialogService triggerdialogService) {
    	this.triggerdialogService = Objects.requireNonNull(triggerdialogService, "TriggerdialogService is null");
    }
}
