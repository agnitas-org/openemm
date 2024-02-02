/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.web;

import static com.agnitas.service.WebStorage.RECIPIENT_OVERVIEW;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_LATEST_DATASOURCE_ID;
import static org.agnitas.emm.core.recipient.RecipientUtils.MAIN_COLUMNS;
import static org.agnitas.emm.core.recipient.RecipientUtils.MAX_SELECTED_FIELDS_COUNT;
import static org.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static org.agnitas.util.Const.Mvc.ERROR_MSG;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static org.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;
import static org.agnitas.web.forms.FormSearchParams.RESET_PARAM_NAME;
import static org.agnitas.web.forms.FormSearchParams.RESTORE_PARAM_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.RecipientUtils;
import org.agnitas.emm.core.recipient.dto.RecipientLightDto;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.recipient.service.SubscriberLimitCheck;
import org.agnitas.emm.core.recipient.service.SubscriberLimitExceededException;
import org.agnitas.emm.core.recipient.service.impl.SubscriberLimitCheckResult;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.target.ConditionalOperator;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.PaginationForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PollingUid;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.delivery.service.DeliveryService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipient.converter.RecipientDtoToRecipientFormConverter;
import com.agnitas.emm.core.recipient.dto.BindingAction;
import com.agnitas.emm.core.recipient.dto.RecipientBindingDto;
import com.agnitas.emm.core.recipient.dto.RecipientBindingsDto;
import com.agnitas.emm.core.recipient.dto.RecipientColumnDefinition;
import com.agnitas.emm.core.recipient.dto.RecipientDto;
import com.agnitas.emm.core.recipient.dto.RecipientFieldDto;
import com.agnitas.emm.core.recipient.dto.RecipientSaveTargetDto;
import com.agnitas.emm.core.recipient.dto.RecipientSearchParamsDto;
import com.agnitas.emm.core.recipient.dto.SaveRecipientDto;
import com.agnitas.emm.core.recipient.forms.RecipientBindingForm;
import com.agnitas.emm.core.recipient.forms.RecipientBindingListForm;
import com.agnitas.emm.core.recipient.forms.RecipientBulkForm;
import com.agnitas.emm.core.recipient.forms.RecipientForm;
import com.agnitas.emm.core.recipient.forms.RecipientListForm;
import com.agnitas.emm.core.recipient.forms.RecipientSimpleActionForm;
import com.agnitas.emm.core.recipient.forms.RecipientsFormSearchParams;
import com.agnitas.emm.core.recipient.service.FieldsSaveResults;
import com.agnitas.emm.core.recipient.service.RecipientLogService;
import com.agnitas.emm.core.target.eql.EqlValidatorService;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderFilterListBuilder;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConverter;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.messages.Message;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.WebStorage;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;

import jakarta.servlet.http.HttpServletRequest;
import net.sf.json.JSONObject;

@SuppressWarnings("all")
public class RecipientController implements XssCheckAware {
	private static final Logger logger = LogManager.getLogger(RecipientController.class);

	private static final String RECIPIENT_LIST_KEY = "recipientList";

	private static final List<String> ALLOWED_FIELDS_WITH_HTML_BY_PERMISSION = new ArrayList<>(Arrays.asList("save", "saveAndBackToList"));

	protected final RecipientService recipientService;
	private final RecipientLogService recipientLogService;
	private final MailinglistApprovalService mailinglistApprovalService;
	protected final ComTargetService targetService;
	private final UserActivityLogService userActivityLogService;
	private final DeliveryService deliveryService;
	private final ComMailingBaseService mailingBaseService;
	protected final WebStorage webStorage;
	private final ConversionService conversionService;
	private QueryBuilderFilterListBuilder filterListBuilder;
    private ColumnInfoService columnInfoService;
	protected ConfigService configService;
	private BlacklistService blacklistService;
	private EqlToQueryBuilderConverter eqlToQueryBuilderConverter;
	private QueryBuilderToEqlConverter queryBuilderToEqlConverter;
	private ComCompanyDao companyDao;
	private EqlValidatorService eqlValidatorService;
	protected SubscriberLimitCheck subscriberLimitCheck;

	public RecipientController(RecipientService recipientService,
							   RecipientLogService recipientLogService,
							   MailinglistApprovalService mailinglistApprovalService,
							   ComTargetService targetService,
							   UserActivityLogService userActivityLogService,
							   DeliveryService deliveryService,
							   ComMailingBaseService mailingBaseService,
							   WebStorage webStorage,
							   ConversionService conversionService,
							   QueryBuilderFilterListBuilder filterListBuilder,
							   ColumnInfoService columnInfoService,
							   ConfigService configService,
							   BlacklistService blacklistService,
							   EqlToQueryBuilderConverter eqlToQueryBuilderConverter,
							   QueryBuilderToEqlConverter queryBuilderToEqlConverter,
							   ComCompanyDao companyDao,
							   EqlValidatorService eqlValidatorService,
							   SubscriberLimitCheck subscriberLimitCheck) {
		this.recipientService = recipientService;
		this.recipientLogService = recipientLogService;
		this.mailinglistApprovalService = mailinglistApprovalService;
		this.targetService = targetService;
		this.userActivityLogService = userActivityLogService;
		this.deliveryService = deliveryService;
		this.mailingBaseService = mailingBaseService;
		this.webStorage = webStorage;
		this.conversionService = conversionService;
		this.filterListBuilder = filterListBuilder;
		this.columnInfoService = columnInfoService;
		this.configService = configService;
		this.blacklistService = blacklistService;
		this.eqlToQueryBuilderConverter = eqlToQueryBuilderConverter;
		this.queryBuilderToEqlConverter = queryBuilderToEqlConverter;
		this.companyDao = companyDao;
		this.eqlValidatorService = eqlValidatorService;
		this.subscriberLimitCheck = subscriberLimitCheck;
	}

	// Please, keep RedirectAttributes to avoid 414 "Request URI too long".
    // Spring passes model attributes to url by default.
    // Using RedirectAttributes we control which attributes to add explicitly. GWUA-4956
	@RequestMapping("/list.action")
	public Object list(Admin admin, @ModelAttribute("listForm") RecipientListForm form, Model model, Popups popups, @RequestHeader HttpHeaders headers,
					   @RequestParam(value = RESET_PARAM_NAME, required = false) boolean resetSearchParams,
					   @RequestParam(value = RESTORE_PARAM_NAME, required = false) boolean restoreSearchParams,
					   @RequestParam(value = "latestDataSourceId", required = false, defaultValue = "0") int dataSourceId,
					   @ModelAttribute RecipientsFormSearchParams recipientsFormSearchParams, RedirectAttributes ra,
                       @ModelAttribute("loadRecipients") String loadRecipientsStr) throws Exception {
		int companyId = admin.getCompanyID();
        boolean loadRecipients = isLoadRecipients(admin, loadRecipientsStr);

		if (resetSearchParams) {
			FormUtils.resetSearchParams(recipientsFormSearchParams, form);
		} else {
			FormUtils.syncSearchParams(recipientsFormSearchParams, form, restoreSearchParams);
		}

		addDataSourceIdRuleIfNecessary(admin, form, dataSourceId, model);

		if (form.getSelectedFields().size() > MAX_SELECTED_FIELDS_COUNT) {
			logger.error("Error getting list of recipients: error.maximum.recipient.columns: count > " + MAX_SELECTED_FIELDS_COUNT);
			popups.alert("error.maximum.recipient.columns");
			return MESSAGES_VIEW;
		}

		form.setEql(queryBuilderToEqlConverter.convertQueryBuilderJsonToEql(form.getSearchQueryBuilderRules(), companyId));

		if (!validateSearchParams(admin, form, popups)) {
			return MESSAGES_VIEW;
		}

		//sync selected fields to display
		FormUtils.syncNumberOfRows(webStorage, RECIPIENT_OVERVIEW, form);
		RecipientUtils.syncSelectedFields(webStorage, RECIPIENT_OVERVIEW, form);

		AgnUtils.setAdminDateTimeFormatPatterns(admin, model);

		PollingUid pollingUid = PollingUid.builder(RequestContextHolder.getRequestAttributes().getSessionId(), RECIPIENT_LIST_KEY)
				.arguments(form.toArray())
				.build();

		model.addAttribute("queryBuilderFilters", filterListBuilder.buildFilterListJson(admin, true));
		model.addAttribute("mailTrackingAvailable", AgnUtils.isMailTrackingAvailable(admin));

		CaseInsensitiveMap<String, ProfileField> columnInfoMap = columnInfoService.getColumnInfoMap(companyId, admin.getAdminID());
		CaseInsensitiveMap<String, ProfileField> columnsVisible = new CaseInsensitiveMap<>();
		for (Entry<String, ProfileField> column : columnInfoMap.entrySet()) {
			if (column.getValue().getModeEdit() == ProfileFieldMode.Editable || column.getValue().getModeEdit() == ProfileFieldMode.ReadOnly) {
				columnsVisible.put(column.getKey(), column.getValue());
			}
		}
		Map<String, String> fields = RecipientUtils.getSortedFieldsMap(columnsVisible);
		model.addAttribute("fieldsMap", fields);
		model.addAttribute("hasAnyDisabledMailingLists", mailinglistApprovalService.hasAnyDisabledMailingListsForAdmin(companyId, admin.getAdminID()));
		model.addAttribute("mailinglists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
		model.addAttribute("loadRecipients", loadRecipients);
		model.addAttribute("isSearchExtended", isSearchExtended(admin));
        addTargetsModelAttrs(admin, model);

		Callable<ModelAndView> worker = () -> {
		    if (loadRecipients) {
                tryGetRecipientsList(admin, form, model, popups, fields);
            }
			writeUserActivityLog(admin, "recipient overview", "active tab - recipient search");
			return new ModelAndView("recipient_list", model.asMap());
		};

		return new Pollable<>(pollingUid, Pollable.LONG_TIMEOUT * 2, new ModelAndView("redirect:/recipient/list.action", form.toMap()), worker);
	}

    protected boolean isSearchExtended(Admin admin) {
        return false; // overwritten in extended file
    }

    protected boolean isLoadRecipients(Admin admin, String loadRecipientsStr) {
        return true; // overwritten in extended file
    }

    protected void addTargetsModelAttrs(Admin admin, Model model) {
        model.addAttribute("targets", targetService.getTargetLights(admin));
    }

    private void tryGetRecipientsList(Admin admin, RecipientListForm form, Model model, Popups popups, Map<String, String> fields) {
        try {
            RecipientSearchParamsDto searchParamsDto = conversionService.convert(form, RecipientSearchParamsDto.class);
            PaginatedListImpl<RecipientDto> paginatedList = recipientService.getPaginatedRecipientList(
                    admin,
                    searchParamsDto,
                    form.getSort(),
                    form.getOrder(),
                    form.getPage(),
                    form.getNumberOfRows(), fields);

            FormUtils.setPaginationParameters(form, paginatedList);
            model.addAttribute(RECIPIENT_LIST_KEY, paginatedList);

            // check the max recipients for company and change visualisation if needed
            int maxRecipients = AgnUtils.getCompanyMaxRecipients(admin);
            if (paginatedList.getFullListSize() > maxRecipients) {
                model.addAttribute("deactivatePagination", true);
                model.addAttribute("countOfRecipients", maxRecipients);

            } else {
                model.addAttribute("deactivatePagination", false);
            }
        } catch (Exception e) {
            logger.error("Getting recipients failed!", e);
            popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
        }
	}

	@PostMapping("/search.action")
	public String search(Admin admin, @ModelAttribute RecipientListForm form,
							   @RequestParam(value = RESET_PARAM_NAME, required = false) boolean resetSearchParams,
							   @ModelAttribute RecipientsFormSearchParams recipientsFormSearchParams,
							   RedirectAttributes model, Popups popups) throws Exception {
		if (resetSearchParams) {
			FormUtils.resetSearchParams(recipientsFormSearchParams, form);
		} else {
			FormUtils.syncSearchParams(recipientsFormSearchParams, form, false);
		}

		model.addFlashAttribute("loadRecipients", true);
		model.addFlashAttribute("listForm", form);

		return "redirect:/recipient/list.action";
	}

	private void addDataSourceIdRuleIfNecessary(Admin admin, RecipientListForm form, int dataSourceId, Model model) {
		int companyId = admin.getCompanyID();
		if (dataSourceId > 0) {
			String lastImportRuleEql = COLUMN_LATEST_DATASOURCE_ID + ConditionalOperator.EQ.getEqlSymbol() + dataSourceId;
			try {
				String lastImportRuleJson = eqlToQueryBuilderConverter.convertEqlToQueryBuilderJson(lastImportRuleEql, companyId);
				form.setSearchQueryBuilderRules(lastImportRuleJson);
				model.addAttribute("forceShowAdvancedSearchTab", true);
			} catch (EqlParserException | EqlToQueryBuilderConversionException e) {
				logger.error("Could not convert query builder rule.", e);
			}
		}
	}

	@PostMapping("/createTargetGroup.action")
	public String createTargetGroup(Admin admin, RecipientSaveTargetForm form, Popups popups, Model model,
								  @ModelAttribute RecipientsFormSearchParams recipientsFormSearchParams) {

		if (isValidTargetSaving(admin, form, popups)) {
			int targetGroupId = saveTargetGroupFromQueryBuilder(form, admin, popups);
			if (targetGroupId > 0) {
				recipientsFormSearchParams.updateTargetId(targetGroupId);
				model.addAttribute("recipientsFormSearchParams", recipientsFormSearchParams);

				popups.success(CHANGES_SAVED_MSG);
				return String.format("redirect:/recipient/list.action?%s=true", RESTORE_PARAM_NAME);
			}
		}

		popups.alert("error.target.saving");
		return MESSAGES_VIEW;
	}

	private int saveTargetGroupFromQueryBuilder(RecipientSaveTargetForm form, Admin admin, Popups popups) {
		List<Message> errors = new ArrayList<>();
		List<UserAction> userActions = new ArrayList<>();
		RecipientSaveTargetDto targetDto = conversionService.convert(form, RecipientSaveTargetDto.class);
		int targetId = targetService.saveTargetFromRecipientSearch(admin, targetDto, errors, userActions);

		if (CollectionUtils.isNotEmpty(errors)) {
			for (Message error : errors) {
				popups.alert(error);
			}
		}

		for (UserAction action : userActions) {
			writeUserActivityLog(admin, action);
		}

		return targetId;
	}


    @GetMapping("/create.action")
    public String create(Admin admin, @ModelAttribute("form") RecipientForm form, Model model, @ModelAttribute RecipientsFormSearchParams recipientsFormSearchParams, Popups popups) {
		int companyID = admin.getCompanyID();
		
    	SubscriberLimitCheckResult subscriberLimitCheckResult = subscriberLimitCheck.checkSubscriberLimit(companyID);
    	if (subscriberLimitCheckResult.isWithinGraceLimitation()) {
    		popups.warning(Message.of(
				"error.numberOfCustomersExceeded.graceful",
				subscriberLimitCheckResult.getMaximumNumberOfCustomers(),
				subscriberLimitCheckResult.getCurrentNumberOfCustomers(),
				subscriberLimitCheckResult.getGracefulLimitExtension()));
    	}
			
		// set data from search params
		RecipientBindingListForm bindingsListForm = form.getBindingsListForm();
		form.setFirstname(recipientsFormSearchParams.getFirstName());
		form.setLastname(recipientsFormSearchParams.getLastName());
		form.setEmail(recipientsFormSearchParams.getEmail());

		if (configService.getBooleanValue(ConfigValue.AnonymizeAllRecipients, companyID)) {
			boolean needToAnonymize = true;
			form.setTrackingVeto(needToAnonymize);
			model.addAttribute("disableTrackingVeto", needToAnonymize);
		}

		loadViewData(admin, model);
		return "recipient_view";
	}

    @GetMapping("/{id:\\d+}/view.action")
    public String view(Admin admin, @PathVariable int id, @ModelAttribute("form") RecipientForm form, Model model) throws RejectAccessByTargetGroupLimit {
		targetService.checkRecipientTargetGroupAccess(admin, id);

		int companyId = admin.getCompanyID();
		RecipientDto recipient = recipientService.getRecipientDto(admin, id);

		if (recipient == null) {
			logger.warn("recipient view: could not load recipient with ID: " + id);
			return "recirect:/recipient/create.action";
		}

		form = RecipientDtoToRecipientFormConverter.convert(recipient, admin);

		loadBindingsData(admin, id, form.getBindingsListForm());
		model.addAttribute("isSaveButtonDisabled", isSaveButtonDisabled(form, admin.getAdminID(), companyId));
		model.addAttribute("form", form);
		loadViewData(admin, model);

		return "recipient_view";
	}

    private boolean isSaveButtonDisabled(RecipientForm form, int adminId, int companyId) {
        return form.getBindingsListForm().getMailinglistBindings().isEmpty()
                && mailinglistApprovalService.hasAnyDisabledMailingListsForAdmin(companyId, adminId);
    }

	private boolean validateSearchParams(Admin admin, RecipientListForm form, Popups popups) throws QueryBuilderToEqlConversionException {
		String eql = form.getEql();

		if (!eql.isBlank()){
			Collection<Message> messages = eqlValidatorService.validateEql(admin, eql);

			if (!messages.isEmpty()) {
				messages.forEach(popups::alert);
				return false;
			}
		}

		return true;
	}

	private void loadBindingsData(Admin admin, int recipientId, RecipientBindingListForm form) {
		Map<Integer, Map<Integer, BindingEntry>> mailinglistBindings = recipientService.getMailinglistBindings(admin.getCompanyID(), recipientId);
		for (Map.Entry<Integer, Map<Integer, BindingEntry>> mlistEntry : mailinglistBindings.entrySet()) {
			RecipientBindingForm bindingForm = form.getListBinding(mlistEntry.getKey());

			Map<MediaTypes, RecipientBindingDto> mediatypeBindings = bindingForm.getMediatypeBindings();
			for (Map.Entry<Integer, BindingEntry> entry : mlistEntry.getValue().entrySet()) {
				mediatypeBindings.put(MediaTypes.getMediaTypeForCode(entry.getKey()), conversionService.convert(entry.getValue(), RecipientBindingDto.class));
			}
		}
	}

	private void loadViewData(Admin admin, Model model) {
		model.addAttribute("columnDefinitions", getRecipientColumnDefinitions(admin));

		AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
		model.addAttribute("isRecipientEmailInUseWarningEnabled", configService.getBooleanValue(ConfigValue.RecipientEmailInUseWarning, admin.getCompanyID()));
		model.addAttribute("mailinglists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
		model.addAttribute("allowedEmptyEmail", configService.getBooleanValue(ConfigValue.AllowEmptyEmail, admin.getCompanyID()));
		model.addAttribute("isMailTrackingEnabled", AgnUtils.isMailTrackingAvailable(admin));
	}

	private List<RecipientColumnDefinition> getRecipientColumnDefinitions(Admin admin) {
		List<RecipientColumnDefinition> definitions = new LinkedList();
		for (ProfileField field : recipientService.getRecipientColumnInfos(admin)) {
			String columnName = StringUtils.isEmpty(field.getColumn()) ? field.getShortname() : field.getColumn();
			// ignore fields with supplemental suffix
			if (!RecipientUtils.hasSupplementalSuffix(columnName)) {
				try {
					RecipientColumnDefinition definition = new RecipientColumnDefinition();
					definition.setColumnName(columnName);
					definition.setMainColumn(MAIN_COLUMNS.contains(columnName));
					definition.setShortname(field.getShortname());
					DbColumnType.SimpleDataType dataType = field.getSimpleDataType();
					definition.setDataType(dataType);
					definition.setEditMode(field.getModeEdit());
					definition.setNullable(field.getNullable());
					definition.setMaxSize(field.getMaxDataSize());
					definition.setLineAfter(BooleanUtils.toBoolean(field.getLine()));

					definition.setDefaultValue(convertColumnValue(admin, dataType, field.getDefaultValue()));
					definition.setFixedValues(convertColumnValues(admin, dataType, field.getAllowedValues()));
					definitions.add(definition);
				} catch(Exception e){
					logger.warn("Could not convert recipient column definition for column: " + columnName, e);
				}
			}
		}

		return definitions;
	}

	private List<String> convertColumnValues(Admin admin, DbColumnType.SimpleDataType dataType, String[] allowedValues) throws Exception {
		List<String> formattedValues = new ArrayList<>();
		for (String value : ArrayUtils.nullToEmpty(allowedValues)) {
			formattedValues.add(convertColumnValue(admin, dataType, value));
		}
		return formattedValues;
	}

	private String convertColumnValue(Admin admin, DbColumnType.SimpleDataType dataType, String defaultValue) throws Exception {
		if (StringUtils.isBlank(defaultValue) || "null".equalsIgnoreCase(defaultValue)) {
			return null;
		} else {
			String formattedValue = defaultValue;

			if (dataType == DbColumnType.SimpleDataType.Date) {
				formattedValue = RecipientUtils.formatRecipientDateValue(admin, defaultValue);
			} else if (dataType == DbColumnType.SimpleDataType.DateTime) {
				formattedValue = RecipientUtils.formatRecipientDateTimeValue(admin, defaultValue);
			} else if (dataType == DbColumnType.SimpleDataType.Float) {
				formattedValue = RecipientUtils.formatRecipientDoubleValue(admin, defaultValue);
			}

			return formattedValue;
		}
	}

	@PostMapping("/checkAltgMatch.action")
	public @ResponseBody BooleanResponseDto isRecipientDataMatchAltgTarget(Admin admin, @ModelAttribute("form") RecipientForm form, Popups popups) {
		boolean result = false;
		if (validate(admin, form, popups)) {
			SaveRecipientDto recipient = conversionService.convert(form, SaveRecipientDto.class);
			SimpleServiceResult isMatchedResult = recipientService.isRecipientMatchAltgTarget(admin, recipient);

			if (isMatchedResult.isSuccess()) {
				return new BooleanResponseDto(popups, true);
			}

			popups.addPopups(isMatchedResult);
		}

		return new BooleanResponseDto(popups, false);
	}

	@PostMapping("/save.action")
    public String save(Admin admin, @ModelAttribute("form") RecipientForm form, Popups popups) {
		if (saveRecipient(admin, form, popups)) {
			return "redirect:/recipient/" + form.getId() + "/view.action";
		}

		return MESSAGES_VIEW;
	}

	@PostMapping("/saveAndBackToList.action")
    public String saveAndBackToList(Admin admin, @ModelAttribute("form") RecipientForm form, Popups popups) {
		if (saveRecipient(admin, form, popups)) {
			return String.format("redirect:/recipient/list.action?%s=true", RESTORE_PARAM_NAME);
		}

		return MESSAGES_VIEW;
	}

	private boolean saveRecipient(Admin admin, RecipientForm form, Popups popups) {
		try {
			int companyId = admin.getCompanyID();
			if (validate(admin, form, popups)) {
				RecipientDto recipientOld = recipientService.getRecipientDto(admin, form.getId());
				boolean isOldEmailBlacklisted = blacklistService.blacklistCheck(recipientOld.getEmail(), companyId);
				boolean isNewEmailBlacklisted = isOldEmailBlacklisted;
				if (!StringUtils.equals(recipientOld.getEmail(), form.getEmail())) {
					isNewEmailBlacklisted = blacklistService.blacklistCheck(form.getEmail(), companyId);
				}

				if (recipientOld.getId() == 0 && configService.getBooleanValue(ConfigValue.AnonymizeAllRecipients, companyId)) {
					form.setTrackingVeto(true);
				}

				if (recipientOld.getId() > 0) {
					form.setEncryptedSend(recipientOld.isEncryptedSend());
				}

				SaveRecipientDto recipient = conversionService.convert(form, SaveRecipientDto.class);
				int datasourceId = companyDao.getCompanyDatasource(companyId);
				if (recipientOld.getId() == 0) {
					recipient.getFieldsToSave().put("datasource_id", Integer.toString(datasourceId));
				}
				recipient.getFieldsToSave().put("latest_datasource_id", Integer.toString(datasourceId));
				List<UserAction> userActions = new ArrayList<>();
				ServiceResult<Integer> saveResult = recipientService.saveRecipient(admin, recipient, userActions);

				if (!saveResult.isSuccess()) {
					popups.addPopups(saveResult);
					return false;
				} else {
					for (Message warning : saveResult.getWarningMessages()) {
						popups.warning(warning);
					}
				}

				form.setId(recipient.getId());
				writeLogs(recipient, recipientOld, admin);
				if (logger.isInfoEnabled()){
					logger.info("save recipient: save recipient " + recipient.getId());
				}

				RecipientBindingsDto bindings = conversionService.convert(form.getBindingsListForm(), RecipientBindingsDto.class);
				bindings.setOldBlacklistedEmail(isOldEmailBlacklisted);
				bindings.setNewBlacklistedEmail(isNewEmailBlacklisted);
				ServiceResult<List<BindingAction>> result = recipientService.saveRecipientBindings(admin, recipient.getId(), bindings, UserStatus.AdminOut);

				if (result.isSuccess()) {
					writeBindingsChangesLog(admin, recipient, result);
				} else if (result.hasErrorMessages()) {
					popups.addPopups(result);
				} else {
					popups.warning("error.recipient.bindings.save");
				}

				popups.success(CHANGES_SAVED_MSG);
				return true;
			}
		} catch(final SubscriberLimitExceededException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Subscriber limit exceeded", e);
			}

			popups.alert("error.maxcustomersexceeded", e.getActual(), e.getMaximum());
		}

		return false;
	}

	private void writeBindingsChangesLog(Admin admin, SaveRecipientDto recipient, ServiceResult<List<BindingAction>> result) {
		List<BindingAction> bindingSaveActions = new ArrayList<>();
		List<BindingAction> bindingUpdateActions = new ArrayList<>();

		for (BindingAction bindingAction : result.getResult()) {
			if (bindingAction.getType().equals(BindingAction.Type.CREATE)) {
				bindingSaveActions.add(bindingAction);
			} else if (bindingAction.getType().equals(BindingAction.Type.UPDATE)) {
				bindingUpdateActions.add(bindingAction);
			}
		}

		writeBindingsLog(admin, recipient, "create recipient bindings", bindingSaveActions);
		writeBindingsLog(admin, recipient, "update recipient bindings", bindingUpdateActions);
	}

	private void writeBindingsLog(Admin admin, SaveRecipientDto recipient, String action, List<BindingAction> actions) {
		if (!actions.isEmpty()) {
			String description = String.format("%s%n%s",
					RecipientUtils.getRecipientDescription(recipient.getId(), recipient.getFirstname(), recipient.getLastname(), recipient.getEmail()),
					actions.stream().map(bindingAction -> bindingAction.getDescription()).collect(Collectors.joining("\n")));

			writeUserActivityLog(admin, new UserAction(action, description));
		}
	}

	private boolean validate(Admin admin, RecipientForm form, Popups popups) {
		boolean isAllowedEmptyEmail = configService.getBooleanValue(ConfigValue.AllowEmptyEmail, admin.getCompanyID());
		if (!isAllowedEmptyEmail && StringUtils.isBlank(form.getEmail())) {
			popups.alert("error.invalid.email");
		}
		if (StringUtils.isNotBlank(form.getEmail()) && !AgnUtils.isEmailValid(form.getEmail())) {
			popups.alert("error.invalid.email");
		}

		if (StringUtils.length(form.getTitle()) > 100) {
			popups.alert("error.recipient.title.tooLong");
		}
		if (StringUtils.length(form.getFirstname()) > 100) {
			popups.alert("error.recipient.firstname.tooLong");
		}
		if (StringUtils.length(form.getLastname()) > 100) {
			popups.alert("error.recipient.lastname.tooLong");
		}

		validateIfNameIsNotLink(form.getFirstname(), "firstname", popups);
		validateIfNameIsNotLink(form.getLastname(), "lastname", popups);

		return !popups.hasAlertPopups();
	}

	private void validateIfNameIsNotLink(String name, String field, Popups popups) {
		if (StringUtils.containsIgnoreCase(name, "http:") ||
				StringUtils.containsIgnoreCase(name, "https:") ||
				StringUtils.containsIgnoreCase(name, "www.")) {
			popups.alert("error.recipient.field.notAllowedLinkData", field);
		}
	}

	@GetMapping(value = "/{recipientId:\\d+}/checkAddress.action")
    public @ResponseBody JSONObject checkAddress(Admin admin, @PathVariable int recipientId, @RequestParam String email) {
		int companyId = admin.getCompanyID();
		JSONObject data = new JSONObject();
        data.element("address", email);
        int existingRecipientId = recipientService.getRecipientIdByAddress(admin, recipientId, email);
        data.element("inUse", existingRecipientId > 0);
        data.element("existingRecipientId", existingRecipientId);
        data.element("isBlacklisted", blacklistService.blacklistCheck(email, companyId));
        return data;
    }

	@GetMapping(value = "/{id:\\d+}/confirmDelete.action")
	public String confirmDelete(Admin admin, @PathVariable int id, @ModelAttribute("form") RecipientSimpleActionForm form, Popups popups) {
		if (!mailinglistApprovalService.hasAnyDisabledRecipientBindingsForAdmin(admin, id)) {
			RecipientLightDto recipient = recipientService.getRecipientLightDto(admin.getCompanyID(), id);
			form.setId(id);
			form.setShortname(recipient.getFirstname() + " " + recipient.getLastname());
			form.setEmail(recipient.getEmail());
			return "recipient_delete";
		}

		popups.alert("error.access.limit.mailinglist");
		return MESSAGES_VIEW;
	}

	@RequestMapping(value = "/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    public String delete(Admin admin, @ModelAttribute("form") RecipientSimpleActionForm form, Popups popups) {
        int id = form.getId();
        if(id > 0 && !mailinglistApprovalService.hasAnyDisabledRecipientBindingsForAdmin(admin, id)){
			recipientService.deleteRecipient(admin.getCompanyID(), id);

            writeUserActivityLog(admin, "delete recipient", RecipientUtils.getRecipientDescription(form.getId(), form.getShortname(), form.getEmail()));
            popups.success(SELECTION_DELETED_MSG);
			return String.format("redirect:/recipient/list.action?%s=true", RESTORE_PARAM_NAME);
        }

		popups.alert("error.access.limit.mailinglist");
        return MESSAGES_VIEW;
    }

	@RequestMapping("/bulkView.action")
    public String bulkView(Admin admin, RecipientBulkForm form, Model model) {
		List<ProfileField> recipientColumns = recipientService.getRecipientBulkFields(admin.getCompanyID(), admin.getAdminID());
		form.setRecipientFieldChanges(recipientColumns.stream().map(ProfileField::getColumn).collect(Collectors.toList()));

		model.addAttribute("recipientColumns", recipientColumns);

		model.addAttribute("hasAnyDisabledMailingLists", mailinglistApprovalService.hasAnyDisabledMailingListsForAdmin(admin));
		model.addAttribute("mailingLists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
		model.addAttribute("targetGroups", targetService.getTargetLights(admin));
		model.addAttribute("calculatedRecipients", recipientService.calculateRecipient(admin, form.getTargetId(), form.getMailinglistId()));

		model.addAttribute("localeDatePattern", admin.getDateFormat().toPattern());

        return "recipient_bulk_change";
    }

    @GetMapping("/calculate.action")
    public @ResponseBody JSONObject calculateRecipients(Admin admin, RecipientBulkForm form) {
		JSONObject result = new JSONObject();
		result.put("targetId", form.getTargetId());
		result.put("mailinglistId", form.getMailinglistId());
		result.put("count", recipientService.calculateRecipient(admin, form.getTargetId(), form.getMailinglistId()));
		return result;
	}

    @PostMapping("/bulkSave.action")
    public String bulkSave(Admin admin, RecipientBulkForm form, RedirectAttributes model, Popups popups) throws Exception {
		if (mailinglistApprovalService.isAdminHaveAccess(admin, form.getMailinglistId())) {
			// saving in case current admin have permission on manage this mailing list

			Map<String, RecipientFieldDto> fieldChanges = form.getRecipientFieldChanges().entrySet().stream()
					.filter(change -> change.getValue().isClear() || StringUtils.isNotEmpty(change.getValue().getNewValue()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			ServiceResult<FieldsSaveResults> saveResult =
					recipientService.saveBulkRecipientFields(admin, form.getTargetId(), form.getMailinglistId(), fieldChanges);

			CaseInsensitiveMap<String, ProfileField> profilefields = columnInfoService.getColumnInfoMap(admin.getCompanyID(), admin.getAdminID());
			for (String columnName : fieldChanges.keySet()) {
				if (!profilefields.containsKey(columnName)) {
					popups.alert("error.import.dbColumnUnknown", columnName);
				} else if (profilefields.get(columnName).getModeEdit() != ProfileFieldMode.Editable) {
					popups.alert("error.import.dbColumnNotEditable", columnName);
				}
			}

			if (saveResult.isSuccess()) {
				writeRecipientBulkChangesLog(admin, form.getTargetId(), form.getMailinglistId(), saveResult.getResult().getAffectedFields());

				int affected = saveResult.getResult().getAffectedRecipients();
				popups.success("bulkAction.changed", affected);
				if(affected > 0) {
					popups.success(CHANGES_SAVED_MSG);
				}
			} else {

				popups.addPopups(saveResult);
			}
		} else {
			popups.warning("warning.mailinglist.disabled");
		}

		model.addFlashAttribute("recipientBulkForm", form);

		return "redirect:/recipient/bulkView.action";
    }

    @GetMapping("/{recipientId:\\d+}/contactHistory.action")
	public String contactHistory(Admin admin, @PathVariable int recipientId, PaginationForm form, Model model, Popups popups) throws RejectAccessByTargetGroupLimit {
		return processRecipientHistoryTab(() -> {
			FormUtils.syncNumberOfRows(webStorage, WebStorage.RECIPIENT_MAILING_HISTORY_OVERVIEW, form);
			model.addAttribute("contactHistoryJson", recipientService.getContactHistoryJson(admin.getCompanyID(), recipientId));
			model.addAttribute("deliveryHistoryEnabled", deliveryService.isDeliveryHistoryEnabled(admin));

			return "recipient_mailings";
		}, admin, recipientId, model, popups, "view mailing history");
	}

	@GetMapping("/{recipientId:\\d+}/mailing/{mailingId:\\d+}/deliveryHistory.action")
	public String deliveryHistory(Admin admin, @PathVariable int recipientId, @PathVariable int mailingId, Model model, Popups popups) throws RejectAccessByTargetGroupLimit {
		targetService.checkRecipientTargetGroupAccess(admin, recipientId);

		try {
			AgnUtils.setAdminDateTimeFormatPatterns(admin, model);

			model.addAttribute("deliveryHistoryJson", deliveryService.getDeliveriesInfo(admin.getCompanyID(), mailingId, recipientId));
			model.addAttribute("mailingShortname", mailingBaseService.getMailingName(mailingId, admin.getCompanyID()));

			return "mailing_delivery_info";
		} catch (Exception e) {
			logger.error("Could not load recipient tab data for companyID: " + admin.getCompanyID(), e);
			popups.alert(ERROR_MSG);
		}

		return MESSAGES_VIEW;
	}

	@GetMapping("/{recipientId:\\d+}/mailing/{mailingId:\\d+}/successfulDeliveryHistory.action")
	public String successfulDeliveryHistory(Admin admin, @PathVariable int recipientId, @PathVariable int mailingId, Model model, Popups popups) throws RejectAccessByTargetGroupLimit {
		int companyID = admin.getCompanyID();
		targetService.checkRecipientTargetGroupAccess(admin, recipientId);

		try {
			AgnUtils.setAdminDateTimeFormatPatterns(admin, model);

			model.addAttribute("deliveryHistoryJson", deliveryService.getSuccessfulDeliveriesInfo(companyID, mailingId, recipientId));
			model.addAttribute("mailingShortname", mailingBaseService.getMailingName(mailingId, companyID));

			return "mailing_successful_delivery_info";
		} catch (Exception e) {
			logger.error("Could not load recipient tab data for companyID: " + companyID, e);
			popups.alert(ERROR_MSG);
		}

		return MESSAGES_VIEW;
	}

	@GetMapping("/{recipientId:\\d+}/statusChangesHistory.action")
	public String statusChangesHistory(Admin admin, @PathVariable int recipientId, PaginationForm form, Model model, Popups popups) throws RejectAccessByTargetGroupLimit {
		return processRecipientHistoryTab(() -> {
			FormUtils.syncNumberOfRows(webStorage, WebStorage.RECIPIENT_STATUS_HISTORY_OVERVIEW, form);

			model.addAttribute("statusChangesHistoryJson", recipientService.getRecipientStatusChangesHistory(admin, recipientId));

			return "recipient_history";
		}, admin, recipientId, model, popups, "view recipient status history");
	}

    protected String processRecipientHistoryTab(Callable<String> action, Admin admin, int recipientId, Model model, Popups popups, String userAction) throws RejectAccessByTargetGroupLimit {
		targetService.checkRecipientTargetGroupAccess(admin, recipientId);

		try {
			AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
			RecipientLightDto recipient = recipientService.getRecipientLightDto(admin.getCompanyID(), recipientId);
			model.addAttribute("recipient", recipient);
			model.addAttribute("isMailTrackingEnabled", AgnUtils.isMailTrackingAvailable(admin));

			writeUserActivityLog(admin, userAction, "For: " + recipient.getEmail() + ". " + RecipientUtils.getRecipientDescription(recipient));

			return action.call();
		} catch (Exception e) {
			logger.error("Could not load recipient tab data for companyID: " + admin.getCompanyID(), e);
			popups.alert(ERROR_MSG);
		}

		return MESSAGES_VIEW;
	}

	private boolean isValidTargetSaving(Admin admin, RecipientSaveTargetForm form, Popups popups) {
		if (!targetService.checkIfTargetNameIsValid(form.getShortname())) {
			popups.alert("error.target.namenotallowed");
		} else if (targetService.checkIfTargetNameAlreadyExists(admin.getCompanyID(), form.getShortname(), 0)) {
			popups.alert("error.target.namealreadyexists");
		} else if (StringUtils.length(form.getShortname()) < 3) {
			popups.alert("error.name.too.short");
		}

		return !popups.hasAlertPopups();
	}


    private void writeRecipientBulkChangesLog(Admin admin, int targetId, int mailitnlistId, Map<String, Object> affectedFields) {
		try {
			UserAction userAction = recipientLogService.getRecipientFieldsBulkChangeLog(targetId, mailitnlistId, affectedFields);
            writeUserActivityLog(admin, userAction);

            if (logger.isInfoEnabled()) {
                logger.info("bulkRecipientFieldEdit: edit field content target ID " + targetId + " and mailit list ID " + mailitnlistId);
            }
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.error("Log Recipient bulk edit error" + e);
            }
        }
	}

	private void writeUserActivityLog(Admin admin, String action, String description) {
        writeUserActivityLog(admin, new UserAction(action, description));
    }

	private void writeLogs(SaveRecipientDto recipient, RecipientDto existedRecipient, Admin admin) {
        if (existedRecipient == null) {
			writeUserActivityLog(admin, "create recipient",
					RecipientUtils.getRecipientDescription(recipient.getId(), recipient.getFirstname(), recipient.getLastname(), recipient.getEmail()));

            if (logger.isInfoEnabled()) {
                logger.info("createRecipient: recipient created " + recipient.getId());
            }
        } else {
            writeRecipientChangesLog(admin, existedRecipient, recipient);
        }
    }

    private void writeRecipientChangesLog(Admin admin, RecipientDto existedRecipient, SaveRecipientDto recipient) {
        try {
            UserAction userAction = recipientLogService.getRecipientChangesLog(admin, existedRecipient, recipient);

            if (Objects.nonNull(userAction)) {
                writeUserActivityLog(admin, userAction);
            }

            if (logger.isInfoEnabled()) {
                logger.info("saveRecipient: save recipient " + existedRecipient.getId());
            }

        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.error("Log Recipient changes error", e);
            }
        }
    }

    private void writeUserActivityLog(Admin admin, UserAction userAction) {
        if (Objects.nonNull(userActivityLogService)) {
            userActivityLogService.writeUserActivityLog(admin, userAction, logger);
        } else {
            logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
            logger.info(String.format("Userlog: %s %s %s", admin.getUsername(), userAction.getAction(),
                    userAction.getDescription()));
        }
    }

	@ModelAttribute
    public RecipientsFormSearchParams getRecipientsFormSearchParams(HttpServletRequest req) {
		return new RecipientsFormSearchParams(getDefaultUserStatusId(AgnUtils.getAdmin(req)));
    }

	@ModelAttribute("listForm")
    public RecipientListForm getListForm(HttpServletRequest req) {
		return new RecipientListForm(getDefaultUserStatusId(AgnUtils.getAdmin(req)));
	}

	private int getDefaultUserStatusId(Admin admin) {
		if (configService.getBooleanValue(ConfigValue.FilterRecipientsOverviewForActiveRecipients, admin.getCompanyID())) {
			return UserStatus.Active.getStatusCode();
		}

		return 0;
	}

	@Override
    public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String parameter, String controllerMethodName) {
		if (controllerMethodName.equals("save") || controllerMethodName.equals("saveAndBackToList")) {
			String columnValue = parameter;
			if (StringUtils.startsWith(parameter, "additionalColumns")) {
				columnValue = StringUtils.substringBetween(parameter, "additionalColumns[", "]");
			}
			if (recipientService.getEditableColumns(admin).get(columnValue) != null) {
				return configService.getBooleanValue(ConfigValue.AllowHtmlInProfileFields);
			}
        }

        return true;
    }
}
