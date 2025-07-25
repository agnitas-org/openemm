/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;

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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingBase;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.MediaTypeStatus;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.factory.MailingComponentFactory;
import com.agnitas.beans.factory.MailingFactory;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.beans.impl.MediatypeEmailImpl;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.dao.MailingComponentDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.forms.MailingTemplateSelectionFilter;
import com.agnitas.emm.core.mailing.service.ListMailingFilter;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.validation.MailingModelValidator;
import com.agnitas.emm.core.mailing.web.MailingSendSecurityOptions;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import com.agnitas.emm.core.mediatypes.dao.MediatypesDaoException;
import com.agnitas.emm.core.mediatypes.factory.MediatypeFactory;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.emm.core.objectusage.service.ObjectUsageService;
import com.agnitas.emm.core.objectusage.web.ObjectUsagesToActionMessages;
import com.agnitas.emm.core.target.exception.TargetNotExistException;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.messages.Message;
import com.agnitas.service.MailingContentService;
import com.agnitas.service.MailingLightService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.FulltextSearchInvalidQueryException;
import com.agnitas.util.HtmlUtils;
import com.agnitas.util.OneOf;
import com.agnitas.util.UserActivityUtil;
import jakarta.mail.internet.InternetAddress;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.commons.util.DateUtil;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.beans.LightweightMailingWithMailingList;
import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.mailing.service.SendDateNotInFutureException;
import org.agnitas.emm.core.mailing.service.TemplateNotExistException;
import org.agnitas.emm.core.mailing.service.WorldMailingAlreadySentException;
import org.agnitas.emm.core.mailing.service.WorldMailingWithoutNormalTypeException;
import org.agnitas.emm.core.mailinglist.service.MailinglistNotExistException;
import org.agnitas.emm.core.mailinglist.service.impl.MailinglistException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

public class MailingServiceImpl implements MailingService, ApplicationContextAware {

	private static final Logger logger = LogManager.getLogger(MailingServiceImpl.class);
	private static final int DEFAULT_LOCKING_DURATION_SECONDS = 60; // 1 minute

	protected MailingDao mailingDao;
	protected ConfigService configService;
    private MailingService selfReference; // self reference for transaction management
	private JavaMailService javaMailService;
	private AdminService adminService;
	private MailinglistDao mailinglistDao;
	private TargetDao targetDao;
	private DynamicTagDao dynamicTagDao;
	private MailingComponentDao mailingComponentDao;
	private MaildropService maildropService;
	private MailingFactory mailingFactory;
	private MailingComponentFactory mailingComponentFactory;
	private MediatypeFactory mediatypeFactory;
	private MailingBaseService mailingBaseService;
	private MailingContentService mailingContentService;
	private MailingModelValidator mailingModelValidator;
	private ApplicationContext applicationContext;
    private ObjectUsageService objectUsageService;
    private UserActivityLogService userActivityLogService;
	private CopyMailingService copyMailingService;
    private MediatypesDao mediatypesDao;
	private BulkActionValidationService<Integer, Mailing> bulkActionValidationService;

	@Override
	@Transactional
	public void updateMailing(MailingModel model, List<UserAction> userActions) throws MailinglistException {
	    mailingModelValidator.assertIsValidToUpdate(model);
        List<String> actions = new LinkedList<>();
        Mailing mailing = prepareMailingForAddOrUpdate(model, getMailing(model), actions, true);

       	List<String> dynNamesForDeletion = new ArrayList<>();
		try {
			mailing.buildDependencies(true, dynNamesForDeletion, applicationContext);
			saveMailingWithNewContent(mailing);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

        mailingDao.updateStatus(mailing.getCompanyID(), mailing.getId(), MailingStatus.EDIT, null);
		dynamicTagDao.markNamesAsDeleted( mailing.getId(), dynNamesForDeletion);

		final String type = (mailing.isIsTemplate() ? "edit template settings" : "edit mailing settings");
		final String description = getMailingDescription(mailing);

        if (actions.isEmpty()) {
        	userActions.add(new UserAction(type, description));
        } else {
            for (String action : actions) {
				userActions.add(new UserAction(type, description + ". " + action));
            }
        }
    }

    @Override
	public int copyMailing(int mailingId, int companyId, String newMailingNamePrefix) throws Exception {
		LightweightMailing lightweightMailing = getLightweightMailing(companyId, mailingId);
		return copyMailingService.copyMailing(companyId, mailingId, companyId, newMailingNamePrefix + lightweightMailing.getShortname(), null);
	}

	@Override
	public int copyMailing(final int newCompanyID, final int newMailinglistID, int fromCompanyID, int fromMailingID, final boolean isTemplate) throws Exception {
		int mailingID = copyMailingService.copyMailing(fromCompanyID, fromMailingID, newCompanyID, null, null);
		Mailing mailingCopy = getMailing(newCompanyID, mailingID);
		mailingCopy.setMailinglistID(newMailinglistID);
		mailingCopy.setIsTemplate(isTemplate);
		saveMailing(mailingCopy, false);
		return mailingCopy.getId();
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
	public MailingStatus getMailingStatus(MailingModel model) {
	    mailingModelValidator.assertIsValidToGet(model);
		int mailingID = model.getMailingId();
		int companyID = model.getCompanyId();

		if(!mailingDao.exist(mailingID, companyID, false)){
			throw new MailingNotExistException(mailingID, mailingID);
		}

		return mailingDao.getStatus(model.getCompanyId(), mailingID);
	}

	/*
	 * TODO: Rework method for transaction support
	 *
	 * Removed @Transactional.
	 * Running this method in a transaction affects the backend, that does
	 * not see the entry created in maildrop_status_tbl.
	 */
    @Override
    public void sendMailing(MailingModel model, List<UserAction> userActions) {
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
                mailingDao.updateStatus(maildrop.getCompanyID(), maildrop.getMailingID(), MailingStatus.ADMIN, null);
            } else if (maildrop.getStatus() == MaildropStatus.TEST.getCode()) {
            	mailingDao.updateStatus(maildrop.getCompanyID(), maildrop.getMailingID(), MailingStatus.TEST, null);
            } else {
                mailingDao.updateStatus(maildrop.getCompanyID(), maildrop.getMailingID(), MailingStatus.SCHEDULED, maildrop.getSendDate());
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
		mailingBaseService.deleteMailing(model.getMailingId(), model.getCompanyId());
        mailingDao.updateStatus(model.getCompanyId(), model.getMailingId(), MailingStatus.DISABLE, null);
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
		if (model.getPlannedDate() != null) {
            aMailing.setPlanDate(model.getPlannedDate());
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
	public boolean isActiveIntervalMailing(int mailingID, int companyId) {
		return mailingDao.isActiveIntervalMailing(companyId, mailingID);
	}

	@Override
	@Transactional
	public MaildropEntry addMaildropEntry(MailingModel model, List<UserAction> userActions) {
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
			aMailing.setCreationDate(new Date());
			aMailing.setDescription(model.getDescription() != null ? model.getDescription() : "");
			aMailing.setShortname(model.getShortname());
			aMailing.setIsTemplate(false);
			aMailing.setMailTemplateID(model.getTemplateId());
            if (model.getPlannedDate() != null) {
                aMailing.setPlanDate(model.getPlannedDate());
            }
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
			for (TrackableLink linkOrg : template.getTrackableLinks().values()) {
				linkOrg.setMeasureSeparately(false);
				TrackableLink linkNew =  cloneBean(linkOrg, "TrackableLink");
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
			saveMailingWithNewContent(aMailing);

	        return aMailing.getId();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T cloneBean(T orig, String beanName) throws IllegalAccessException, InvocationTargetException {
		return cloneBean((T) applicationContext.getBean(beanName), orig); // suppress warning for this cast
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
	public boolean exists(int mailingID, int companyID) {
		return mailingDao.exist(mailingID, companyID);
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
	public Map<Integer, List<LightweightMailingWithMailingList>> findActionBasedMailingsForMailinglists(List<Mailinglist> mailinglists, int companyId) {
		Set<Integer> mailinglistIds = mailinglists.stream()
				.map(Mailinglist::getId)
				.collect(Collectors.toSet());

		return listAllActionBasedMailingsForMailinglists(mailinglistIds, companyId)
				.stream()
				.collect(Collectors.groupingBy(LightweightMailingWithMailingList::getMailingListId));
	}

	@Override
	public List<LightweightMailingWithMailingList> listAllActionBasedMailingsForMailinglist(final int companyID, int mailinglistID) {
		return listAllActionBasedMailingsForMailinglists(Set.of(mailinglistID), companyID);
	}

	private List<LightweightMailingWithMailingList> listAllActionBasedMailingsForMailinglists(Set<Integer> ids, int companyId) {
		return mailingDao.listAllActionBasedMailingsForMailinglists(ids, companyId);
	}

	@Override
	public final LightweightMailing getLightweightMailing(final int companyId, final int mailingId) throws MailingNotExistException {
		return this.mailingDao.getLightweightMailing(companyId, mailingId);
	}

	@Override
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
	public boolean isFollowupMailingDateBeforeDate(Mailing mailing, Date boundDate) {
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
		mailingDao.updateStatus(companyID, mailingID, status, null);
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

		mailingContentService.generateTextContent(mailing);

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
	public List<MailingBase> getTemplatesWithPreview(MailingTemplateSelectionFilter filter, Admin admin) {
		return mailingDao.getMailingTemplatesWithPreview(filter, admin);
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
	public int saveMailingWithNewContent(Mailing mailing) throws Exception {
		activateTrackingEachLinkIfNecessary(mailing);
		return saveMailing(mailing, false);
	}

	@Override
	public int saveMailingWithNewContent(Mailing mailing, Admin admin) throws Exception {
		boolean trackingEachLinkActivated = activateTrackingEachLinkIfNecessary(mailing);
		return saveMailing(mailing, !trackingEachLinkActivated && admin.permissionAllowed(Permission.MAILING_TRACKABLELINKS_NOCLEANUP));
	}

	@Override
	public int saveMailingWithNewContent(Mailing mailing, boolean preserveTrackableLinks, boolean errorTolerant, boolean removeUnusedContent) throws Exception {
		activateTrackingEachLinkIfNecessary(mailing);
		return mailingDao.saveMailing(mailing, preserveTrackableLinks, errorTolerant, removeUnusedContent);
	}

	private boolean activateTrackingEachLinkIfNecessary(Mailing mailing) throws Exception {
		if (!configService.getBooleanValue(ConfigValue.IndividualLinkTrackingForAllMailings, mailing.getCompanyID())) {
			return false;
		}

		Optional<Date> changeDate = configService.getChangeDate(ConfigValue.IndividualLinkTrackingForAllMailings, mailing.getCompanyID());
		if (changeDate.isEmpty()) {
			return false;
		}

		if (changeDate.get().compareTo(Optional.ofNullable(mailing.getCreationDate()).orElse(new Date())) < 0) {
			mailingBaseService.activateTrackingLinksOnEveryPosition(mailing, applicationContext);
			return true;
		}

		return false;
	}

	@Override
	public int saveMailing(Mailing mailing, boolean preserveTrackableLinks) {
		return mailingDao.saveMailing(mailing, preserveTrackableLinks);
	}

    @Override
    public List<UserAction> deleteMailing(int mailingId, Admin admin) {
        Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());
        mailingBaseService.deleteMailing(mailingId, admin.getCompanyID());
        return Collections.singletonList(getDeleteUserAction(mailing));
    }

    @Override
    @Transactional
    public ServiceResult<List<UserAction>> bulkDelete(Collection<Integer> ids, Admin admin) {
        ServiceResult<List<Mailing>> mailingsResult = selfReference.getMailingsForDeletion(ids, admin);
        if (!mailingsResult.isSuccess()) {
            return ServiceResult.error(mailingsResult.getErrorMessages());
        }

		if (admin.isRedesignedUiUsed()) {
			// It only changes the 'deleted' column value
			Set<Integer> allowedIds = mailingsResult.getResult().stream()
					.map(Mailing::getId)
					.collect(Collectors.toSet());
			mailingBaseService.bulkDelete(allowedIds, admin.getCompanyID());
		} else {
			// It only changes the 'deleted' column value
			mailingBaseService.bulkDelete(new HashSet<>(ids), admin.getCompanyID());
		}

        return ServiceResult.success(mailingsResult.getResult().stream()
                .map(this::getDeleteUserAction)
                .toList());
    }

    private UserAction getDeleteUserAction(Mailing mailing) {
        return new UserAction(
                "delete " + (mailing.isIsTemplate() ? "template" : "mailing"),
                mailing.getShortname() + " (" + mailing.getId() + ")");
    }

    @Override
    public boolean usedInRunningWorkflow(final int mailingId, final int companyId) {
        return mailingDao.usedInRunningWorkflow(mailingId, companyId);
    }

	@Override
	public Date getMailingLastSendDate(int mailingId) {
		return mailingDao.getLastSendDate(mailingId);
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
				saveMailingWithNewContent(mailing);
            } catch (Exception e) {
                logger.error("unable to update mailing ID {}", mailingId, e);
            }
        }
    }

    @Override
    public boolean isBaseMailingTrackingDataAvailable(int baseMailingId, Admin admin) {
        return mailingDao.getMailings(admin.getCompanyID(), admin.getAdminID(),
                MailingLightService.TAKE_ALL_MAILINGS, "W", true).stream()
                .anyMatch(mailing -> mailing.getId() == baseMailingId);
    }

    @Override
    public boolean checkMailingReferencesTemplate(int templateId, int companyId) {
		return mailingDao.checkMailingReferencesTemplate(templateId, companyId);
    }

    @Override
    public boolean isDynamicTemplateCheckboxVisible(Mailing mailing) {
        if (mailing.isIsTemplate()) {
            // For templates checkbox is always show and enabled
            return true;
        } else if (mailing.getMailTemplateID() != 0) {
            // For mailings, checkbox is always shows if and only if referenced mailing-record defines template
            // Checkbox is only enabled, if such a mailing has ID 0 (new mailing)
            return checkMailingReferencesTemplate(mailing.getMailTemplateID(), mailing.getCompanyID());
        } else {
            // in all other cases, the checkbox is hidden
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

	@Override
	public List<Integer> findTargetDependentMailings(int targetGroupId, int companyId) {
		return mailingDao.findTargetDependentMailings(targetGroupId, companyId);
	}

	@Override
	public List<Integer> filterNotSentMailings(List<Integer> mailings) {
		if (mailings.isEmpty()) {
			return Collections.emptyList();
		}

		return mailingDao.filterNotSentMailings(mailings);
	}

	@Override
    public boolean isBasicFullTextSearchSupported() {
        return mailingDao.isBasicFullTextSearchSupported();
    }

	@Override
    public boolean isContentFullTextSearchSupported() {
        return mailingDao.isContentFullTextSearchSupported();
    }

    @Override
    public ServiceResult<PaginatedListImpl<Map<String, Object>>> getOverview(Admin admin, MailingsListProperties props) {
        try {
            return ServiceResult.success(mailingDao.getMailingList(admin, props));
        } catch (FulltextSearchInvalidQueryException e) {
            return ServiceResult.error(e.getUiMessage());
        }
    }

    @Override
    public List<Map<String, String>> listTriggers(int mailingId, int companyId) {
	    return mailingDao.loadAction(mailingId, companyId);
    }

    @Override
    @Transactional
    public ServiceResult<List<Mailing>> getMailingsForDeletion(Collection<Integer> mailingIds, Admin admin) {
        if (CollectionUtils.isEmpty(mailingIds)) {
            return ServiceResult.error(Message.of(NOTHING_SELECTED_MSG));
        }

		if (admin.isRedesignedUiUsed()) {
			return bulkActionValidationService.checkAllowedForDeletion(mailingIds, id -> getMailingForDeletion(id, admin));
		} else {
			List<ServiceResult<Mailing>> results = mailingIds.stream()
					.map(mailingId -> getMailingForDeletion(mailingId, admin))
					.collect(Collectors.toList());
			List<Message> errors = results.stream()
					.map(ServiceResult::getErrorMessages)
					.filter(Objects::nonNull)
					.flatMap(Collection::stream)
					.collect(Collectors.toList());
			List<Message> warns = results.stream()
					.map(ServiceResult::getWarningMessages)
					.filter(Objects::nonNull)
					.flatMap(Collection::stream)
					.collect(Collectors.toList());
			return new ServiceResult<>(
					results.stream().map(ServiceResult::getResult).collect(Collectors.toList()),
					errors.isEmpty(), Collections.emptyList(), warns, errors);
		}
    }

    private SimpleServiceResult checkIfMailingUsed(int mailingId, Admin admin) {
        List<Message> errors = new ArrayList<>();
        List<Message> warnings = new ArrayList<>();
        ObjectUsages objectUsages = objectUsageService.listUsageOfMailing(admin.getCompanyID(), mailingId);
        if (!objectUsages.isEmpty()) {
            errors.add(ObjectUsagesToActionMessages.objectUsagesToMessage("error.mailing.used",
                    "error.mailing.used.withMore", objectUsages, admin.getLocale()));
        }
        if (usedInRunningWorkflow(mailingId, admin.getCompanyID())) {
            errors.add(Message.of("error.workflow.mailingUsedInActiveWorkflow"));
        }
        if (!listFollowupMailingIds(admin.getCompanyID(), mailingId, true).isEmpty()) {
            warnings.add(Message.of("warning.mailing.delete.followup.target"));
        }
        return new SimpleServiceResult(errors.isEmpty(), null, warnings, errors);
    }

    @Override
    @Transactional
    public ServiceResult<Mailing> getMailingForDeletion(int mailingId, Admin admin) {
        try {
            Mailing mailing = getMailing(admin.getCompanyID(), mailingId);
            SimpleServiceResult usingResult = checkIfMailingUsed(mailingId, admin);
            return new ServiceResult<>(mailing, usingResult.isSuccess(),
                    null,
                    usingResult.getWarningMessages(),
                    usingResult.getErrorMessages());
        } catch (MailingNotExistException ex) {
            return ServiceResult.errorKeys("error.general.missing");
        }
    }

	@Override
	@Transactional
	public void restoreMailing(int mailingId, Admin admin) {
		restoreMailings(Set.of(mailingId), admin);
	}

	@Override
	@Transactional
	public void bulkRestore(Collection<Integer> mailingIds, Admin admin) {
		restoreMailings(mailingIds, admin);
	}

	protected void restoreMailings(Collection<Integer> ids, Admin admin) {
		mailingDao.restoreMailings(ids, admin.getCompanyID());
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
    public Mailing getMailing(int mailingId, int companyId, boolean includeDependencies) {
        return mailingDao.getMailing(mailingId, companyId, includeDependencies);
    }

    @Override
    public Map<Integer, Mediatype> getMediatypes(int mailingId, int companyId) throws MediatypesDaoException {
        return mediatypesDao.loadMediatypes(mailingId, companyId);
    }

	@Override
	public boolean isDateBasedMailingWasSentToday(int mailingId) {
		return mailingDao.isDateBasedMailingWasSentToday(mailingId);
	}

	@Override
	public void allowDateBasedMailingResending(int mailingId) {
		mailingDao.allowDateBasedMailingResending(mailingId);
	}

	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}

	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}

	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}

	public void setTargetDao(TargetDao targetDao) {
		this.targetDao = targetDao;
	}

	public void setDynamicTagDao(DynamicTagDao dynamicTagDao) {
		this.dynamicTagDao = dynamicTagDao;
	}

	public void setMailingComponentDao(MailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}

	public void setMaildropService(MaildropService maildropService) {
		this.maildropService = maildropService;
	}

	public void setMailingFactory(MailingFactory mailingFactory) {
		this.mailingFactory = mailingFactory;
	}

	public void setMailingComponentFactory(MailingComponentFactory mailingComponentFactory) {
		this.mailingComponentFactory = mailingComponentFactory;
	}

	public void setMediatypeFactory(MediatypeFactory mediatypeFactory) {
		this.mediatypeFactory = mediatypeFactory;
	}

	public void setMailingBaseService(MailingBaseService mailingBaseService) {
		this.mailingBaseService = mailingBaseService;
	}

	public void setMailingContentService(MailingContentService mailingContentService) {
		this.mailingContentService = mailingContentService;
	}

	public void setMailingModelValidator(MailingModelValidator mailingModelValidator) {
		this.mailingModelValidator = mailingModelValidator;
	}

    public void setObjectUsageService(ObjectUsageService objectUsageService) {
        this.objectUsageService = objectUsageService;
    }

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

    public void setUserActivityLogService(UserActivityLogService userActivityLogService) {
        this.userActivityLogService = userActivityLogService;
    }

	public void setCopyMailingService(CopyMailingService copyMailingService) {
		this.copyMailingService = copyMailingService;
	}

    public void setMediatypesDao(MediatypesDao mediatypesDao) {
        this.mediatypesDao = mediatypesDao;
    }

	public void setBulkActionValidationService(BulkActionValidationService<Integer, Mailing> bulkActionValidationService) {
		this.bulkActionValidationService = bulkActionValidationService;
	}

    public void setSelfReference(MailingService mailingService) {
        this.selfReference = mailingService;
    }

	@Override
	public MailingStatus getMailingStatus(int companyID, int id) {
        return mailingDao.getStatus(companyID, id);
	}

	@Override
	public boolean hasMailingStatus(int mailingId, MailingStatus status, int companyID) {
		return status.equals(getMailingStatus(companyID, mailingId));
	}

	@Override
	public boolean saveMailingDescriptiveData(Mailing mailing) {
		return mailingDao.saveMailingDescriptiveData(mailing);
	}

	@Override
	public List<LightweightMailing> getMailingsUsingEmmAction(int actionId, int companyID) {
		return mailingDao.getMailingsUsingEmmAction(actionId, companyID);
	}

    @Override
    public void clearPlanDate(int mailingId, int companyId) {
        mailingDao.clearPlanDate(mailingId, companyId);
    }

	@Override
	public String getEmailParameter(int mailingID) {
		return mailingDao.getEmailParameter(mailingID);
	}

	@Override
	public String getMailingRdirDomain(int mailingID, int companyID) {
		return mailingDao.getMailingRdirDomain(mailingID, companyID);
	}

	@Override
	public MailingType getMailingType(int mailingID) {
		return mailingDao.getMailingType(mailingID);
	}

	@Override
	public boolean isSettingsReadonly(Admin admin, int mailingId) {
		return isSettingsReadonly(admin, mailingDao.isTemplate(mailingId, admin.getCompanyID()));
	}

	@Override
	public boolean isSettingsReadonly(Admin admin, boolean isTemplate) {
		return admin.permissionAllowed(Permission.MAILING_CONTENT_READONLY)
				|| !admin.permissionAllowed(isTemplate ? Permission.TEMPLATE_CHANGE : Permission.MAILING_CHANGE);
	}

	@Override
	public boolean isMarkedAsDeleted(int mailingId, int companyID) {
		return mailingDao.isMarkedAsDeleted(mailingId, companyID);
	}

	@Override
	public boolean isTemplate(int mailingId, int companyId) {
		return mailingDao.isTemplate(mailingId, companyId);
	}
}
