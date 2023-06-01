/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.agnitas.emm.core.mailing.service.validation.MailingModelValidator;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.beans.factory.MailingComponentFactory;
import org.agnitas.beans.factory.MailingFactory;
import org.agnitas.dao.MailingStatus;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.commons.util.DateUtil;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.mailing.service.SendDateNotInFutureException;
import org.agnitas.emm.core.mailing.service.TemplateNotExistException;
import org.agnitas.emm.core.mailing.service.WorldMailingAlreadySentException;
import org.agnitas.emm.core.mailing.service.WorldMailingWithoutNormalTypeException;
import org.agnitas.emm.core.mailinglist.service.MailinglistNotExistException;
import org.agnitas.emm.core.mailinglist.service.impl.MailinglistException;
import org.agnitas.emm.core.mediatypes.factory.MediatypeFactory;
import org.agnitas.emm.core.target.service.TargetNotExistException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.DynTagException;
import org.agnitas.util.HtmlUtils;
import org.agnitas.util.UserActivityUtil;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.beans.impl.MediatypeEmailImpl;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.ListMailingFilter;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.web.MailingSendSecurityOptions;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.service.ComMailingContentService;
import com.agnitas.service.ComMailingLightService;
import com.agnitas.util.OneOf;

import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;

public class MailingServiceImpl implements MailingService, ApplicationContextAware {
	// TODO Remove ApplicationContextAware
	
	/** The logger. */
	private static final Logger logger = LogManager.getLogger(MailingServiceImpl.class);
	private static final int DEFAULT_LOCKING_DURATION_SECONDS = 60; // 1 minute

	protected ConfigService configService;
    
	private JavaMailService javaMailService;

	protected ApplicationContext applicationContext;

	protected AdminService adminService;

	@Resource(name="MailingDao")
	protected ComMailingDao mailingDao;
	
	@Resource(name="MailinglistDao")
	private MailinglistDao mailinglistDao;
	
	@Resource(name="TargetDao")
	private ComTargetDao targetDao;

	/** DAO for accessing dyn-tags. */
	@Resource(name="DynamicTagDao")
	private DynamicTagDao dynamicTagDao;
	
	/** DAO accessing mailing components. */
	private ComMailingComponentDao mailingComponentDao;
	
	@Resource(name="MaildropService")
	private MaildropService maildropService;

	@Resource(name = "MailingFactory")
	private MailingFactory mailingFactory;

	@Resource(name = "MailingComponentFactory")
	private MailingComponentFactory mailingComponentFactory;

	@Resource(name="MediatypeFactory")
	private MediatypeFactory mediatypeFactory;
	
	@Resource(name="MailingBaseService")
	private ComMailingBaseService mailingBaseService;
	
	@Resource(name="MailingContentService")
	protected ComMailingContentService mailingContentService;

	private MailingModelValidator mailingModelValidator;

	private UserActivityLogService userActivityLogService;


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * Set configuration service.
	 * 
	 * @param configService configuration service
	 */
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}

	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Required
	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}

	@Required
	public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
		this.mailingBaseService = mailingBaseService;
	}
	
	@Required
	public void setMailingContentService(ComMailingContentService mailingContentService) {
		this.mailingContentService = mailingContentService;
	}
	
	@Required
	public void setMaildropService(final MaildropService maildropService) {
		this.maildropService = Objects.requireNonNull(maildropService, "maildropService");
	}
	
    @Required
  	public void setMailingModelValidator(MailingModelValidator mailingModelValidator) {
  		this.mailingModelValidator = mailingModelValidator;
  	}

	@Required
	public void setUserActivityLogService(UserActivityLogService userActivityLogService) {
		this.userActivityLogService = userActivityLogService;
	}
	
	public final void setMailinglistDao(final MailinglistDao dao) {
		this.mailinglistDao = Objects.requireNonNull(dao, "MailinglistDao");
	}

	@Override
	public boolean isApproved(int mailingId, int companyId) {
		return mailingDao.isApproved(mailingId, companyId);
	}

	@Override
	public void removeApproval(int mailingId, Admin admin) {
		int companyID = admin.getCompanyID();

		if (isApproved(mailingId, companyID)) {
			mailingDao.removeApproval(mailingId, companyID);
			writeRemoveApprovalLog(mailingId, admin);
		}
	}

	@Override
	public void writeRemoveApprovalLog(int mailingId, Admin admin) {
		String mailingName = mailingDao.getMailingName(mailingId, admin.getCompanyID());

		UserActivityUtil.log(
				userActivityLogService,
				admin,
				"approval",
				String.format("Approval removed for mailing %s (%d)", mailingName, mailingId),
				logger
		);
		logger.warn(
				"Approval removed for mailing {} ({}) by {}.",
				mailingName,
				mailingId,
				admin.getFullUsername()
		);
	}


	@Override
	@Transactional
	public void updateMailing(MailingModel model, List<UserAction> userActions) throws MailinglistException {
	    mailingModelValidator.assertIsValidToUpdate(model);
        List<String> actions = new LinkedList<>();
        Mailing aMailing = prepareMailingForAddOrUpdate(model, getMailing(model), actions, true);
		
       	List<String> dynNamesForDeletion = new Vector<>();
		try {
			aMailing.buildDependencies(true, dynNamesForDeletion, applicationContext);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
       	mailingDao.saveMailing(aMailing, false);
        mailingDao.updateStatus(aMailing.getId(), MailingStatus.EDIT);

		dynamicTagDao.markNamesAsDeleted( aMailing.getId(), dynNamesForDeletion);

		final String type = (aMailing.isIsTemplate() ? "edit template settings" : "edit mailing settings");
		final String description = getMailingDescription(aMailing);

        if (actions.isEmpty()) {
        	userActions.add(new UserAction(type, description));
        } else {
            for (String action : actions) {
				userActions.add(new UserAction(type, description + ". " + action));
            }
        }
    }

    @Override
	public List<LightweightMailing> getAllMailingNames(Admin admin) {
		return mailingDao.getMailingNames(admin);
	}

	@Override
	public int getMailGenerationMinutes(int companyID) {
		return configService.getIntegerValue(ConfigValue.MailGenerationTimeMinutes, companyID);
	}

	@Override
	public List<Mailing> getDuplicateMailing(List<WorkflowIcon> icons, int companyId) {
		Set<Integer> metIds = new HashSet<>();

        return icons.stream()
				.filter(this::isNonReusableMailingIcon)
                .map(WorkflowUtils::getMailingId)
                .filter(id -> id > 0 && !metIds.add(id))  // Ignore unique ids.
                .map(id -> mailingDao.getMailing(id, companyId))
                .collect(Collectors.toList());
	}

	/**
	 * Check if a given icon represents a mailing of non-reusable type (same mailing can't be used in more than one icon).
	 *
	 * @param icon a workflow icon to check.
	 * @return {@code true} if icon represents a mailing of non-reusable type or {@code false} otherwise.
	 */
	private boolean isNonReusableMailingIcon(WorkflowIcon icon) {
		switch (icon.getType()) {
			case WorkflowIconType.Constants.MAILING_ID:
			case WorkflowIconType.Constants.FOLLOWUP_MAILING_ID:
				return true;
			default:
				return false;
		}
	}

	@Override
	@Transactional
	public String getMailingStatus(MailingModel model) {
	    mailingModelValidator.assertIsValidToGet(model);
		int mailingID = model.getMailingId();
		int companyID = model.getCompanyId();

		if(!mailingDao.exist(mailingID, companyID, false)){
			throw new MailingNotExistException(mailingID, mailingID);
		}

		return mailingDao.getWorkStatus(model.getCompanyId(), mailingID);
	}

    @Override
    public void sendMailing(MailingModel model, List<UserAction> userActions) throws Exception {
	    mailingModelValidator.assertIsValidToSend(model);
    	MaildropEntry maildrop = addMaildropEntry(model, userActions);
        Mailing mailing = getMailing(model);
        if (maildrop.getGenStatus() == 1) {
            if (model.getMaildropStatus() == MaildropStatus.WORLD
                    && mailing.getMailingType() != MailingType.NORMAL) {
                throw new WorldMailingWithoutNormalTypeException();
            }

            mailing.triggerMailing(maildrop.getId());
        }

        if (!DateUtil.isDateForImmediateGeneration(maildrop.getGenDate()) && ((mailing.getMailingType() == MailingType.NORMAL) ||
                (mailing.getMailingType() == MailingType.FOLLOW_UP))) {
            if (maildrop.getStatus() == MaildropStatus.ADMIN.getCode()) {
                mailingDao.updateStatus(maildrop.getMailingID(), MailingStatus.ADMIN);
            } else if (maildrop.getStatus() == MaildropStatus.TEST.getCode()) {
            	mailingDao.updateStatus(maildrop.getMailingID(), MailingStatus.TEST);
            } else {
                mailingDao.updateStatus(maildrop.getMailingID(), MailingStatus.SCHEDULED);
            }
        }
    }

	@Override
	@Transactional
	public void deleteMailing(MailingModel model) {
        mailingModelValidator.assertIsValidToGet(model);
		if (!mailingDao.exist(model.getMailingId(), model.getCompanyId(), model.isTemplate())) {
			throw model.isTemplate() ? new TemplateNotExistException() : new MailingNotExistException(model.getCompanyId(), model.getMailingId());
		}
		mailingDao.deleteMailing(model.getMailingId(), model.getCompanyId());
        mailingDao.updateStatus(model.getMailingId(), MailingStatus.DISABLE);
	}
	
	@Override
	public List<MailingComponent> getMailingComponents(int mailingID, int companyID)
			throws MailingNotExistException {
		
		Mailing mailing = mailingDao.getMailing(mailingID, companyID);
		
		if (mailing == null || mailing.getId() != mailingID || mailing.getCompanyID() != companyID) {
			throw new MailingNotExistException(companyID, mailingID);
		}
		
		return mailingComponentDao.getMailingComponents(mailingID, companyID, MailingComponentType.Template);
	}

	/**
	 * Set DAO accessing mailing components.
	 * 
	 * @param mailingComponentDao DAO accessing mailing components
	 */
	public void setMailingComponentDao(ComMailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}
	
	protected Mailing prepareMailingForAddOrUpdate(MailingModel model, Mailing aMailing, List<String> actions, final boolean isUpdate) throws MailinglistNotExistException {
        final String editKeyword = "edit ";
        StringBuilder actionMessage = new StringBuilder(editKeyword);
		aMailing.setCompanyID(model.getCompanyId());
		aMailing.setDescription(model.getDescription() != null ? model.getDescription() : "");
		aMailing.setShortname(model.getShortname());
		aMailing.setIsTemplate(model.isTemplate());

		if (model.getMailinglistId() != 0) {
			if (!mailinglistDao.exist(model.getMailinglistId(), model.getCompanyId())) {
				throw new MailinglistNotExistException(model.getMailingId(), model.getCompanyId());
			}
            if (actions != null && aMailing.getMailinglistID() != model.getMailinglistId()) {
                actionMessage.append("mailing list changed from ").append(aMailing.getMailinglistID()).append(" to ").append(model.getMailinglistId());
                if (model.getMailinglistId() == 0) {
                	//send mail
                	String message = "Mailinglist ID in mailing (" + aMailing.getId() + ") was set to 0. Please check if the content still exists!";
                	javaMailService.sendEmail(aMailing.getCompanyID(), configService.getValue(ConfigValue.Mailaddress_Error), "Mailinglist set to 0", message, HtmlUtils.replaceLineFeedsForHTML(message));
                }
                actions.add(actionMessage.toString());
                actionMessage.delete(editKeyword.length(), actionMessage.length());
            }
			aMailing.setMailinglistID(model.getMailinglistId());
		}

		aMailing.setTargetMode(model.getTargetMode().getValue());
		if (model.getTargetIDList() != null && !model.getTargetIDList().isEmpty()) {
			Set<Integer> tGroups = new HashSet<>();
			for (Integer targetID : model.getTargetIDList()) {
				if (targetDao.getTarget(targetID, model.getCompanyId()) == null) {
					throw new TargetNotExistException(targetID);
				}
				tGroups.add(targetID);
			}
            if (actions != null) {
                Set<Integer> oldGroups = new HashSet<>();
                if(aMailing.getTargetGroups() != null) {
                    oldGroups.addAll(aMailing.getTargetGroups());
                }
                Set<Integer> newGroups = new HashSet<>(tGroups);
                newGroups.removeAll(oldGroups);
                oldGroups.removeAll(tGroups);
                if(!oldGroups.isEmpty()){
                    actionMessage.append("removed ");
                    for (Integer next : oldGroups) {
                        actionMessage.append(next).append(", ");
                    }
                }
                if(!newGroups.isEmpty()){
                    actionMessage.append("added ");
                    for (Integer next : newGroups) {
                        actionMessage.append(next).append(", ");
                    }
                }
            }
            aMailing.setTargetGroups(tGroups);
        } else {
            if(actions != null && aMailing.getTargetGroups() != null && !aMailing.getTargetGroups().isEmpty()){
                actionMessage.append("removed ");
                for (Integer next : aMailing.getTargetGroups()) {
                    actionMessage.append(next).append(", ");
                }
            }
			aMailing.setTargetGroups(null);
		}
		
		if (actions != null && actionMessage.length() != editKeyword.length()) {
			actionMessage.delete(actionMessage.length()-2,actionMessage.length()); //remove last two characters: comma and space
			actionMessage.insert(editKeyword.length(),"target groups ");
			actions.add(actionMessage.toString());
			actionMessage.delete(editKeyword.length(), actionMessage.length());
		}

        if (actions != null && aMailing.getMailingType() != model.getMailingType()){
            actionMessage.append("mailing type from ")
                    .append(aMailing.getMailingType().name())
                    .append(" to ")
                    .append(model.getMailingType().name());
            actions.add(actionMessage.toString());
            actionMessage.delete(editKeyword.length(), actionMessage.length());
        }
        aMailing.setMailingType(model.getMailingType());

        
        MediatypeEmail paramEmail = aMailing.getEmailParam();
        
        if (paramEmail == null) {
        	paramEmail = new MediatypeEmailImpl();
        	
        	if(!isUpdate) {
        		paramEmail.setStatus(MediaTypeStatus.Active.getCode());
        	}
        	
			final Map<Integer, Mediatype> mediatypes = aMailing.getMediatypes();
        	mediatypes.put(0, paramEmail);
			aMailing.setMediatypes(mediatypes);
        }
        
		paramEmail.setSubject(model.getSubject());
		try {
			InternetAddress adr = new InternetAddress(model.getSenderAddress(), model.getSenderName());
			paramEmail.setFromEmail(adr.getAddress());
			paramEmail.setFromFullname(adr.getPersonal());

			InternetAddress reply = new InternetAddress(model.getReplyToAddress(), model.getReplyToName());
			paramEmail.setReplyEmail(reply.getAddress());
			paramEmail.setReplyFullname(reply.getPersonal());
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException in sender/reply address", e);
			throw new RuntimeException(e);
		}
		paramEmail.setCharset(model.getCharset());
		paramEmail.setMailFormat(model.getFormat().getValue());
		paramEmail.setLinefeed(model.getLinefeed());
		paramEmail.setPriority(1);

		paramEmail.setOnepixel(model.getOnePixel().getName());

		return aMailing;
	}

	/**
	 * Construct mailing description acceptable for user log
	 * @param mailing a mailing instance.
	 * @return mailing description including name and ID.
	 */
	protected String getMailingDescription(Mailing mailing) {
		return mailing.getShortname() + " (" + mailing.getId() + ")";
	}
	
	@Override
	public boolean isActiveIntervalMailing(final int mailingID) {
		return mailingDao.isActiveIntervalMailing(mailingID);
	}

	@Override
	@Transactional
	public MaildropEntry addMaildropEntry(MailingModel model, List<UserAction> userActions) throws Exception {
        mailingModelValidator.assertIsValidToSend(model);
		if (!DateUtil.isValidSendDate(model.getSendDate())) {
			throw new SendDateNotInFutureException();
		}
		
        Calendar now = Calendar.getInstance();
        
		Mailing mailing = getMailing(model);
		
		if (model.getMaildropStatus() == MaildropStatus.WORLD && maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID())) {
			throw new WorldMailingAlreadySentException();
		}

		MaildropEntry maildrop = new MaildropEntryImpl();

		maildrop.setStatus(model.getMaildropStatus().getCode());
		maildrop.setMailingID(model.getMailingId());
		maildrop.setCompanyID(model.getCompanyId());
        maildrop.setStepping(model.getStepping());
		maildrop.setBlocksize(model.getBlocksize());

		maildrop.setSendDate(model.getSendDate());
		
		Calendar tmpGen = Calendar.getInstance();
        tmpGen.setTime(model.getSendDate());
        tmpGen.add(Calendar.MINUTE, -this.getMailGenerationMinutes(model.getCompanyId()));
        if(tmpGen.before(now)) {
            tmpGen=now;
        }
        maildrop.setGenDate(tmpGen.getTime());
		maildrop.setGenChangeDate(now.getTime());
		
		if( model.getMaildropStatus() == MaildropStatus.WORLD) {
			maildrop.setGenStatus(DateUtil.isDateForImmediateGeneration(maildrop.getGenDate()) ? 1 : 0);
		} else if( model.getMaildropStatus() == MaildropStatus.TEST || model.getMaildropStatus() == MaildropStatus.ADMIN) {
			maildrop.setGenStatus( 1);
		}

        mailing.getMaildropStatus().add(maildrop);

        mailingDao.saveMailing(mailing, false);
        if (logger.isInfoEnabled()) {
        	logger.info("send mailing id: " + mailing.getId() + " type: "+maildrop.getStatus());
        }

        SimpleDateFormat dateTimeFormat = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.UK);
        dateTimeFormat.applyPattern(dateTimeFormat.toPattern().replaceFirst("y+", "yyyy").replaceFirst(", ", " "));
		String strDate = dateTimeFormat.format(model.getSendDate());
		String description = String.format("Date: %s. Mailing %s(%d) %s", strDate, mailing.getShortname(), mailing.getId(), "normal");

        userActions.add(new UserAction("edit send date", description));

        return maildrop;
	}

	@Override
	public boolean isMailingWorldSent(int mailingID, int companyID) throws MailingNotExistException {
		Mailing mailing = mailingDao.getMailing(mailingID, companyID);
		
		if(mailing == null || mailing.getId() == 0) {
			throw new MailingNotExistException(companyID, mailingID);
		}

		return this.maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID());
	}
	
	@Override
	@Transactional
	public int addMailingFromTemplate(MailingModel model) {
	    mailingModelValidator.assertIsValidToAddFromTemplate(model);
    	Mailing template = mailingDao.getMailing(model.getTemplateId(), model.getCompanyId());

    	if (template == null || !template.isIsTemplate()) {
    		throw new TemplateNotExistException();
    	}

		try {
			Mailing aMailing = cloneBean(template, "Mailing");

			aMailing.setId(0);
			aMailing.setDescription(model.getDescription() != null ? model.getDescription() : "");
			aMailing.setShortname(model.getShortname());
			aMailing.setIsTemplate(false);
			aMailing.setMailTemplateID(model.getTemplateId());

			// copy components
			for (MailingComponent compOrg : template.getComponents().values()) {
				MailingComponent compNew = cloneBean(compOrg, "MailingComponent");
				compNew.setId(0);
				compNew.setMailingID(0);
				
				if (compOrg.getBinaryBlock() != null) {
					compNew.setBinaryBlock(compOrg.getBinaryBlock(), compOrg.getMimeType());
				} else {
					compNew.setEmmBlock(compOrg.getEmmBlock(), compOrg.getMimeType());
				}
				
				aMailing.getComponents().put(compNew.getComponentName(), compNew);
			}

			// copy dyntags
			for (DynamicTag tagOrg : template.getDynTags().values()) {
				DynamicTag tagNew = cloneBean(tagOrg, "DynamicTag");
				tagNew.setId(0);
				tagNew.setMailingID(0);
				tagNew.setDynContent(new HashMap<>());
				for (DynamicTagContent contentOrg : tagOrg.getDynContent().values()) {
					DynamicTagContent contentNew = cloneBean(contentOrg, "DynamicTagContent");
					contentNew.setId(0);
					contentNew.setDynNameID(0);
					tagNew.addContent(contentNew);
				}
				aMailing.getDynTags().put(tagNew.getDynName(), tagNew);
			}

			// copy urls
			for (ComTrackableLink linkOrg : template.getTrackableLinks().values()) {
				linkOrg.setMeasureSeparately(false);
				ComTrackableLink linkNew =  cloneBean(linkOrg, "TrackableLink");
				linkNew.setId(0);
				linkNew.setMailingID(0);
				aMailing.getTrackableLinks().put(linkNew.getFullUrl(), linkNew);
			}

			// copy mediatypes
			for (Entry<Integer, Mediatype> entry : template.getMediatypes().entrySet()) {
				int mediaTypeCode = entry.getKey();

				if (mediatypeFactory.isTypeSupported(mediaTypeCode)) {
					Mediatype mediatypeNew = mediatypeFactory.create(mediaTypeCode);
					aMailing.getMediatypes().put(mediaTypeCode, cloneBean(mediatypeNew, entry.getValue()));
				}
			}

			aMailing.setUseDynamicTemplate(model.isAutoUpdate());

	        mailingDao.saveMailing(aMailing, false);

	        return aMailing.getId();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T cloneBean(T orig, String beanName) throws IllegalAccessException, InvocationTargetException {
		return cloneBean((T) applicationContext.getBean(beanName), orig);
	}

	private <T> T cloneBean(T dest, T orig) throws IllegalAccessException, InvocationTargetException {
		PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(orig);
		for (PropertyDescriptor descriptor : origDescriptors) {
			String name = descriptor.getName();
			if (PropertyUtils.isReadable(orig, name) && PropertyUtils.isWriteable(dest, name)) {
				try {
					Object value = PropertyUtils.getSimpleProperty(orig, name);
					if (!(value instanceof Collection<?>) && !(value instanceof Map<?, ?>)) {
						PropertyUtils.setSimpleProperty(dest, name, value);
					}
				} catch (NoSuchMethodException e) {
					logger.debug("Error writing to '" + name + "' on class '" + dest.getClass() + "'", e);
				}
			}
		}
		return dest;
	}

	@Override
	@Transactional
	public Mailing getMailing(MailingModel model) {
        mailingModelValidator.assertIsValidToGet(model);
		Mailing mailing = mailingDao.getMailing(model.getMailingId(), model.getCompanyId());
		if (model.isTemplate() && (mailing == null || mailing.getId() == 0 || !mailing.isIsTemplate())) {
			throw new TemplateNotExistException();
		} else if (!model.isTemplate() && (mailing == null || mailing.getId() == 0 || mailing.isIsTemplate())) {
			throw new MailingNotExistException(model.getCompanyId(), model.getMailingId());
		}
		return mailing;
	}
	
	@Override
	@Transactional
	public Mailing getMailing(final int companyID, final int mailingID) {
		final Mailing mailing = mailingDao.getMailing(mailingID, companyID);
		
		if (mailing == null || mailing.getId() == 0) {
			throw new MailingNotExistException(companyID, mailingID);
		} else {
			return mailing;
		}
	}
	
	@Override
    public int getFollowUpFor(int mailingId) {
        try {
            return Integer.parseInt(mailingDao.getFollowUpFor(mailingId));
        } catch (Exception e) {
            return -1;
        }
    }

	@Override
	@Transactional
	public int addMailing(MailingModel model) throws MailinglistNotExistException {
	    mailingModelValidator.assertIsValidToAdd(model);
		int result;
		Mailing aMailing = prepareMailingForAddOrUpdate(model, mailingFactory.newMailing(), null, false);

		MailingComponent comp;

		comp = mailingComponentFactory.newMailingComponent();
		comp.setCompanyID(model.getCompanyId());
		comp.setComponentName("agnText");
		comp.setType(MailingComponentType.Template);
		comp.setEmmBlock("[agnDYN name=\"emailText\"/]", "text/plain");
		aMailing.addComponent(comp);

		comp = mailingComponentFactory.newMailingComponent();
		comp.setCompanyID(model.getCompanyId());
		comp.setComponentName("agnHtml");
		comp.setType(MailingComponentType.Template);
		comp.setEmmBlock("[agnDYN name=\"emailHtml\"/]", "text/html");
		aMailing.addComponent(comp);

		try {
			aMailing.buildDependencies(true, applicationContext);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

		mailingDao.saveMailing(aMailing, false);
		result = aMailing.getId();
		return result;
	}

	@Override
	public List<Mailing> getMailings(MailingModel model) {
	    mailingModelValidator.assertCompany(model.getCompanyId());
		return mailingDao.getMailings(model.getCompanyId(), model.isTemplate());
	}

	@Override
	public List<Mailing> listMailings(final int companyId, final ListMailingFilter filter) {
	    mailingModelValidator.assertCompany(companyId);
		return filter != null
				? mailingDao.listMailings(companyId, false, filter)
				: mailingDao.getMailings(companyId, false);
	}

	@Override
	@Transactional
	public List<Mailing> getMailingsForMLID(MailingModel model) throws MailinglistException {
	    mailingModelValidator.assertIsValidToGetForMLId(model);
		if (!mailinglistDao.exist(model.getMailinglistId(), model.getCompanyId())) {
			throw new MailinglistNotExistException(model.getMailinglistId(), model.getCompanyId());
		}
		return mailingDao.getMailingsForMLID(model.getCompanyId(), model.getMailinglistId());
	}

	@Override
	public boolean switchStatusmailOnErrorOnly(int companyId, int mailingId, boolean statusmailOnErrorOnly) {
		if (mailingId <= 0 || !mailingDao.exist(mailingId, companyId)) {
			return false;
		} else {
			return mailingDao.saveStatusmailOnErrorOnly(companyId, mailingId, statusmailOnErrorOnly);
		}
	}

	@Override
	public boolean resumeDateBasedSending(int companyId, int mailingId) {
		if (companyId > 0 && mailingDao.exist(mailingId, companyId)) {
			return mailingDao.resumeDateBasedSending(mailingId);
		}

		return false;
	}

	@Override
	public boolean saveSecuritySettings(int companyId, int mailingId, MailingSendSecurityOptions options) {
		if (companyId > 0 && mailingDao.exist(mailingId, companyId)) {
			return mailingDao.saveSecuritySettings(companyId, mailingId, options);
		}

		return false;
	}

	@Override
	public boolean isTextVersionRequired(int companyId, int mailingId) {
		return mailingDao.isTextVersionRequired(companyId, mailingId);
	}

	@Override
	public List<LightweightMailing> listAllActionBasedMailingsForMailinglist(final int companyID, int mailinglistID) {
		return this.mailingDao.listAllActionBasedMailingsForMailinglist(companyID, mailinglistID);
	}

	@Override
	public final LightweightMailing getLightweightMailing(final int companyId, final int mailingId) throws MailingNotExistException {
		return this.mailingDao.getLightweightMailing(companyId, mailingId);
	}

	@Override
	@Transactional
	public List<TargetLight> listTargetGroupsOfMailing(int companyID, int mailingID) throws MailingNotExistException {
		Mailing mailing = getMailing(companyID, mailingID);
		Collection<Integer> targetIdList = mailing.getAllReferencedTargetGroups();

		return targetDao.getTargetLights(companyID, targetIdList, false);
	}

	@Override
	public boolean containsInvalidTargetGroups(int companyID, int mailingId) {
		try {
			List<TargetLight> targetGroups = listTargetGroupsOfMailing(companyID, mailingId);
			return targetGroups.stream().anyMatch(tl -> !tl.isValid());
		} catch (MailingNotExistException e) {
			return false;
		}
	}

	@Override
	public String getTargetExpression(final int companyId, final int mailingId) {
		return mailingDao.getTargetExpression(mailingId, companyId);
	}

	@Override
	@Transactional
	public boolean tryToLock(Admin admin, int mailingId) {
		return mailingDao.tryToLock(mailingId, admin.getAdminID(), admin.getCompanyID(), DEFAULT_LOCKING_DURATION_SECONDS, TimeUnit.SECONDS);
	}
	
	@Override
	public Admin getMailingLockingAdmin(int mailingId, int companyId) {
	    int lockingAdminId = mailingDao.getMailingLockingAdminId(mailingId, companyId);
	    return adminService.getAdmin(lockingAdminId, companyId);
    }
	
	@Override
	public boolean isDeliveryComplete(final int companyID, final int mailingID) {
		final LightweightMailing mailing = this.mailingDao.getLightweightMailing(companyID, mailingID);

		return isDeliveryComplete(mailing);
	}

	@Override
	public boolean isMissingNecessaryTargetGroup(Mailing mailing) {
		return !mailing.isIsTemplate() && mailing.getNeedsTarget() && (mailing.getTargetGroups() == null || mailing.getTargetGroups().isEmpty());
	}

	@Override
	public boolean isFollowupMailingDateBeforeDate(Mailing mailing, Date boundDate) throws Exception {
		if (mailing == null) {
			logger.warn("Try to validate the date for non existing followup mailing!");
			return false;
		}

		if (mailing.getMailingType() == MailingType.FOLLOW_UP) {
			// Check basemailing data for followup mailing
			String followUpFor = mailingDao.getFollowUpFor(mailing.getId());

			if (StringUtils.isNotEmpty(followUpFor)) {
				int baseMailingId = Integer.parseInt(followUpFor);

				Timestamp baseMailingSendDate = mailingDao.getLastSendDate(baseMailingId);
				if (baseMailingSendDate == null || boundDate.before(baseMailingSendDate)) {
					return true;
				}
			}
		}

		logger.warn("Try to validate the date for non followup mailing (" + mailing.getId() + ")");
		return false;
	}

	@Override
	public boolean isMailingLocked(Mailing mailing) {
		return mailing == null || mailing.getLocked() != 0;
	}

	@Override
	public final boolean isDeliveryComplete(final LightweightMailing mailing) {
		return mailing.getWorkStatus().isPresent() && OneOf.oneObjectOf(mailing.getWorkStatus().get(), MailingStatus.SENT.getDbKey(), MailingStatus.NORECIPIENTS.getDbKey());
	}

	@Override
	public void updateStatus(int companyID, int mailingID, MailingStatus status) {
		this.mailingDao.updateStatus(mailingID, status);
	}

	@Override
	public List<Integer> listFollowupMailingIds(int companyID, int mailingID, boolean includeUnscheduled) {
		return this.mailingDao.getFollowupMailings(mailingID, companyID, includeUnscheduled);
	}

    @Override
    public boolean generateMailingTextContentFromHtml(Admin admin, int mailingId) throws Exception {
		Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());
		if (mailing == null) {
			return false;
		}
		
		try {
			mailingContentService.generateTextContent(mailing);
		} catch (DynTagException e) {
			logger.error("Error occurred while generating text content: " + e.getMessage(), e);
			return false;
		}
		
		List<String> dynNamesForDeletion = new ArrayList<>();
		try {
			mailing.buildDependencies(true, dynNamesForDeletion, applicationContext);
		} catch (Exception e) {
			logger.error("Error occurred while building dependencies: " + e.getMessage(), e);
        	throw e;
		}
		
		dynamicTagDao.cleanupContentForDynNames(mailingId, admin.getCompanyID(), dynNamesForDeletion);
		dynamicTagDao.markNamesAsDeleted(mailingId, dynNamesForDeletion);
		mailingBaseService.saveMailingWithUndo(mailing, admin.getAdminID(), false);
		
		return true;
    }

	@Override
	public List<LightweightMailing> getLightweightMailings(final Admin admin) {
		return mailingDao.getLightweightMailings(admin.getCompanyID());
	}

	@Override
	public List<LightweightMailing> getLightweightIntervalMailings(final Admin admin) {
		return mailingDao.getLightweightIntervalMailings(admin);
	}

	@Override
	public List<LightweightMailing> getMailingsDependentOnTargetGroup(final int companyID, final int id) {
		return mailingDao.getMailingsDependentOnTargetGroup(companyID, id);
	}

	@Override
	public List<Mailing> getTemplates(Admin admin) {
		return mailingDao.getTemplates(admin);
	}

	@Override
	public List<MailingBase> getTemplatesWithPreview(final Admin admin, final String sort, final String direction) {
		return mailingDao.getMailingTemplatesWithPreview(admin, sort, direction);
	}

    @Override
    public List<MailingBase> getMailingsByStatusE(int companyId) {
        if (companyId > 0) {
        	return mailingDao.getMailingsByStatusE(companyId);
		} else {
        	return new ArrayList<>();
		}
    }

    @Override
	public List<LightweightMailing> getUnsetMailingsForRootTemplate(int companyId, int templateId) {
		return mailingDao.getUnsetMailingsForRootTemplate(companyId, templateId);
	}

    @Override
    public boolean isThresholdClearanceExceeded(int companyId, int mailingId) {
		return mailingDao.isThresholdClearanceExceeded(mailingId);
    }

	@Override
	public int saveMailing(Mailing mailing, boolean preserveTrackableLinks) {
		return mailingDao.saveMailing(mailing, preserveTrackableLinks);
	}
	
    @Override
    public List<UserAction> deleteMailing(int mailingId, Admin admin) throws Exception {
        Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());
        mailingDao.deleteMailing(mailingId, admin.getCompanyID());
        return Collections.singletonList(new UserAction("delete " + (mailing.isIsTemplate() ? "template" : "mailing"), mailing.getShortname() + " (" + mailing + ")"));
    }
    
    @Override
    public boolean usedInRunningWorkflow(final int mailingId, final int companyId) {
        return mailingDao.usedInRunningWorkflow(mailingId, companyId);
    }
    
    @Override
    public void updateMailingsWithDynamicTemplate(Mailing template, ApplicationContext context) {
        List<Integer> referencingMailings = mailingDao.getTemplateReferencingMailingIds(template);
        if (CollectionUtils.isEmpty(referencingMailings)) {
            return;
        }

        MailingComponent srcText = template.getTextTemplate();
        MailingComponent srcHtml = template.getHtmlTemplate();

        Mailing mailing;
        MailingComponent mailingComponent;

        for (int mailingId : referencingMailings) {
            mailing = mailingDao.getMailing(mailingId, template.getCompanyID());

            // First, handle text template
            mailingComponent = mailing.getTextTemplate();

            // Modify text template only if mailing and template have both a text template
            if (srcText != null && mailingComponent != null) {
                mailingComponent.setEmmBlock(srcText.getEmmBlock(), "text/plain");
            }

            // Next, handle HTML template
            mailingComponent = mailing.getHtmlTemplate();

            // Modify HTML template only if mailing and template have both a HTML template
            if (srcHtml != null && mailingComponent != null) {
                mailingComponent.setEmmBlock(srcHtml.getEmmBlock(), "text/html");
            }

            try {
                mailing.buildDependencies(true, context);
                mailingDao.saveMailing(mailing, false);
            } catch (Exception e) {
                logger.error("unable to update mailing ID {}", mailingId, e);
            }
        }
    }
    
    @Override
    public boolean isBaseMailingTrackingDataAvailable(int baseMailingId, Admin admin) {
        return mailingDao.getMailings(admin.getCompanyID(), admin.getAdminID(),
                ComMailingLightService.TAKE_ALL_MAILINGS, "W", true).stream()
                .anyMatch(mailing -> mailing.getId() == baseMailingId);
    }
    
    @Override
    public Date getMailingPlanDate(final int mailingId, final int companyId) {
        return mailingDao.getMailingPlanDate(mailingId, companyId);
    }
    
    @Override
    public boolean checkMailingReferencesTemplate(int templateId, int companyId) {
        try {
            return mailingDao.checkMailingReferencesTemplate(templateId, companyId);
        } catch (Exception e) {
            return false;
        }
    }

	@Override
    public boolean hasMediaType(int mailingId, MediaTypes type, int companyId) {
	    return mailingDao.hasMediaType(companyId, mailingId, type);    
    }

	@Override
	public String getMailingName(int mailingId, int companyId) {
		return mailingDao.getMailingName(mailingId, companyId);
	}

	@Override
	public boolean isMailingTargetsHaveConjunction(Admin admin, Mailing mailing) {
		return mailing.getTargetMode() == Mailing.TARGET_MODE_AND;
	}

	// Is required, but is injected by field injection in general
    public final void setMailingFactory(final MailingFactory factory) {
    	this.mailingFactory = Objects.requireNonNull(factory, "mailing factory");
    }
    
    // Is required, but is injected by field injection in general 
    public final void setMailingComponentFactory(final MailingComponentFactory factory) {
    	this.mailingComponentFactory = Objects.requireNonNull(factory, "mailing component factory");
    }
    
    // Is required, but is injected by field injection in general 
    public final void setTargetDao(final ComTargetDao dao) {
    	this.targetDao = Objects.requireNonNull(dao, "target dao");
    }

	@Override
	public MailingStatus getMailingStatus(int companyID, int id) {
		String workStatusString = mailingDao.getWorkStatus(companyID, id);
		return MailingStatus.fromDbKey(workStatusString);
	}

	@Override
	public boolean saveMailingDescriptiveData(Mailing mailing) {
		return mailingDao.saveMailingDescriptiveData(mailing);
	}
}
