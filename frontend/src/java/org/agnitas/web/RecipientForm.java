/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.BindingEntry;
import org.agnitas.emm.core.recipient.dto.RecipientFrequencyCounterDto;
import org.agnitas.target.ConditionalOperator;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.SafeString;
import org.agnitas.web.forms.StrutsFormBase;
import org.agnitas.web.forms.TargetEqlBuilder;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import com.agnitas.emm.core.recipient.service.RecipientType;

public class RecipientForm extends StrutsFormBase  {
	private static final long serialVersionUID = -1626162472029428066L;

	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(RecipientForm.class);

    protected int action = 9;
	protected int recipientID = 0;
    protected int gender;
    protected int mailtype = 1;
    protected int user_status;
    protected int listID;

    protected String title = "";
    protected String firstname = "";
    protected String lastname = "";
    protected String email = "";
    protected String user_type = "";

	private String searchFirstName = "";
	private String searchLastName = "";
	private String searchEmail = "";

    private String[] selectedFields = ArrayUtils.EMPTY_STRING_ARRAY;
    protected Map<String, Object> column = new CaseInsensitiveMap<>();

    protected Map<Integer, Map<Integer, BindingEntry>> mailing = new HashMap<>();

    protected int targetID;
    protected int mailinglistID = 0;

    protected boolean overview = true;	// recipient overview or recipient search?

    protected ActionMessages messages;
    protected ActionErrors errors;

    protected boolean deactivatePagination;

    protected boolean fromListPage;

    protected int adminId;
    private int mailingId;
    private String mailingName;

    private RecipientFrequencyCounterDto frequencyCounterDto;


    /**
     * Flag is used to save target group from advanced search fields
     */
    private boolean needSaveTargetGroup = false;

    @Deprecated
    private boolean advancedSearch;
    @Deprecated
    private boolean changeToAdvancedSearch;

    @Deprecated
    protected TargetEqlBuilder targetEqlBuilder;

    private String queryBuilderRules;
    private int latestDataSourceId;


    public RecipientForm() {
    	targetEqlBuilder = new TargetEqlBuilder();
    }

    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     * @return errors
     */
    @Override
	public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
        errors = new ActionErrors();

        if (request.getParameter("trgt_clear") != null) {
            setRecipientID(0);
            clearRules();


            if (action != RecipientAction.ACTION_LIST ){ // reset filter fields only if there is no future running
            	setUser_status(0);
            	setUser_type("");
            	setTargetID(0);
               	setListID(0);
                setQueryBuilderRules("[]");
            }
        }

        return errors;
    }

    @Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
    	super.reset(mapping, request);

        targetEqlBuilder.reset();
        needSaveTargetGroup = false;
        // We need just to check if user is logged in - in other case we'll get NPE here.
        // The actual redirect to login page will be made by Action class.
        if (AgnUtils.isUserLoggedIn(request)) {
            targetEqlBuilder.setShortname(SafeString.getLocaleString("default.Name", AgnUtils.getLocale(request)));
            targetEqlBuilder.setDescription(SafeString.getLocaleString("default.description", AgnUtils.getLocale(request)));
        }

        setNumberOfRows(-1);
        selectedFields = ArrayUtils.EMPTY_STRING_ARRAY;

        mailingId = 0;
        mailingName = null;
    }
    
    public void resetSearch() {
        setSearchFirstName(StringUtils.EMPTY);
		setSearchEmail(StringUtils.EMPTY);
		setSearchLastName(StringUtils.EMPTY);
		setListID(0);
		setTargetID(0);
		setUser_status(0);
        setUser_type("");
        setQueryBuilderRules("[]");
		cleanAllRules();
    }

    @Deprecated
    private void cleanAllRules() {
        for (int index = targetEqlBuilder.getNumTargetNodes(); index >= 0; index--) {
            removeRule(index);
        }
    }

    @Deprecated
    public void clearRules() {
        targetEqlBuilder.clearRules();
    }


	/**
     * Getter for property action.
     *
     * @return Value of property action.
     */
    public int getAction() {
        return this.action;
    }

    /**
     * Setter for property action.
     *
     * @param action New value of property action.
     */
    public void setAction(int action) {
        this.action = action;
    }

	/**
     * Getter for property recipientID.
     *
     * @return Value of property recipientID.
     */
    public int getRecipientID() {
        return this.recipientID;
    }

    /**
     * Setter for property recipientID.
     *
     * @param recipientID New value of property recipientID.
     */
    public void setRecipientID(int recipientID) {
        this.recipientID=recipientID;
    }

    /**
     * Getter for property gender.
     *
     * @return Value of property gender.
     */
    public int getGender() {
        return this.gender;
    }

    /**
     * Setter for property gender.
     *
     * @param gender New value of property gender.
     */
    public void setGender(int gender) {
        this.gender=gender;
    }

    /**
     * Getter for property mailtype.
     *
     * @return Value of property mailtype.
     */
    public int getMailtype() {
        return this.mailtype;
    }

    /**
     * Setter for property mailtype.
     *
     * @param mailtype New value of property mailtype.
     */
    public void setMailtype(int mailtype) {
        this.mailtype=mailtype;
    }

    /**
     * Getter for property user_status.
     *
     * @return Value of property user_status.
     */
    public int getUser_status() {
        return this.user_status;
    }

    /**
     * Setter for property user_status.
     *
     * @param user_status New value of property user_status.
     */
    public void setUser_status(int user_status) {
        this.user_status=user_status;
    }

    /**
     * Getter for property listID.
     *
     * @return Value of property listID.
     */
    public int getListID() {
        return this.listID;
    }

    /**
     * Setter for property listID.
     *
     * @param listID New value of property listID.
     */
    public void setListID(int listID) {
        this.listID=listID;
    }

    /**
     * Getter for property title.
     *
     * @return Value of property title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Setter for property title.
     *
     * @param title New value of property title.
     */
    public void setTitle(String title) {
        this.title=title;
    }

    /**
     * Getter for property firstname.
     *
     * @return Value of property firstname.
     */
    public String getFirstname() {
        return this.firstname;
    }

    /**
     * Setter for property firstname.
     *
     * @param firstname New value of property firstname.
     */
    public void setFirstname(String firstname) {
        this.firstname=firstname;
    }

    /**
     * Getter for property lastname.
     *
     * @return Value of property lastname.
     */
    public String getLastname() {
        return this.lastname;
    }

    /**
     * Setter for property lastname.
     *
     * @param lastname New value of property lastname.
     */
    public void setLastname(String lastname) {
        this.lastname=lastname;
    }

    /**
     * Getter for property email.
     *
     * @return Value of property email.
     */
    public String getEmail() {
        return this.email.toLowerCase();
    }

    /**
     * Setter for property email.
     *
     * @param email New value of property email.
     */
    public void setEmail(String email) {
        this.email=email;
    }

	/**
	 * Getter for property searchFirstName
	 *
	 * @return value of property searchFirstName
	 */
	public String getSearchFirstName() {
		return searchFirstName;
	}

	/**
	 * Setter for property searchFirstName
	 *
	 * @param searchFirstName new value for property searchFirstName
	 */
	public void setSearchFirstName(String searchFirstName) {
		this.searchFirstName = searchFirstName;
	}

	/**
	 * Getter for property searchLastName
	 *
	 * @return value of property searchLastName
	 */
	public String getSearchLastName() {
		return searchLastName;
	}

	/**
	 * Setter for property searchLastName
	 *
	 * @param searchLastName new value for property searchLastName
	 */
	public void setSearchLastName(String searchLastName) {
		this.searchLastName = searchLastName;
	}

	/**
	 * Getter for property searchEmail
	 *
	 * @return value of property searchEmail
	 */
	public String getSearchEmail() {
		return searchEmail;
	}

	/**
	 * Setter for property searchEmail
	 *
	 * @param searchEmail new value for property searchEmail
	 */
	public void setSearchEmail(String searchEmail) {
		this.searchEmail = searchEmail;
	}

	/**
     * Getter for property user_type.
     *
     * @return Value of property user_type.
     */
    public String getUser_type() {
        return this.user_type;
    }
    
    public boolean isDefaultUserType() {
        return StringUtils.isBlank(user_type) || user_type.equals(RecipientType.ALL_RECIPIENTS.getLetter());
    }

    /**
     * Setter for property user_type.
     *
     * @param user_type New value of property user_type.
     */
    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }


    /**
     * Getter for property columnMap.
     *
     * @return Value of property columnsMap.
     */
    public Map<String, Object> getColumnMap() {
        return column;
    }

    public void clearColumns() {
        setFirstname("");
        setLastname("");
        setTitle("");
        setEmail("");
        setGender(0);

        this.column.clear();
    }

    /**
     * Getter for property columns.
     *
     * @return Value of property column.
     */
    public Object getColumn(String key) {
        return column.get(key);
    }

    /**
     * Setter for property column.
     *
     * @param key The name of the column to set.
     * @param value New value for the column.
     */
    public void setColumn(String key, Object value) {
        column.put(key, value);
    }

    /**
     * Getter for property bindingEntry.
     *
     * @return Value of property bindingEntry.
     */
    public BindingEntry getBindingEntry(int ignoreType, int id) {
        Map<Integer, BindingEntry> sub =
                mailing.computeIfAbsent(id, unused -> new HashMap<>());

        return sub.computeIfAbsent(0, unused -> {
            BindingEntry entry = getWebApplicationContext().getBean("BindingEntry", BindingEntry.class);
            entry.setMailinglistID(id);
            entry.setMediaType(0);
            return entry;
        });
    }

    /**
     * Setter for property bindingEntry.
     *
     * @param id New value of property bindingEntry.
     */
    public void setBindingEntry(int id, BindingEntry entry) {
        int type = entry.getMediaType();
        BindingEntry bindingEntry = getBindingEntry(type, id);

        if (bindingEntry != null) {
            Map<Integer, BindingEntry> sub = mailing.computeIfAbsent(id, unused -> new HashMap<>());
            sub.put(type, entry);
        }
    }

    /**
     * Getter for property allBindings.
     *
     * @return Value of property allBindings.
     */
    public Map<Integer, Map<Integer, BindingEntry>> getAllBindings() {
        return mailing;
    }

	public int getTargetID() {
		return targetID;
	}

	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}

	public int getMailinglistID() {
		return mailinglistID;
	}

	public void setMailinglistID(int mailinglistID) {
		this.mailinglistID = mailinglistID;
	}

	public boolean isDeactivatePagination() {
        return deactivatePagination;
    }

    public void setDeactivatePagination(boolean deactivatePagination) {
        this.deactivatePagination = deactivatePagination;
    }

    public boolean getFromListPage() {
        return fromListPage;
    }

    public void setFromListPage(boolean fromListPage) {
        this.fromListPage = fromListPage;
    }

    public void setSelectedFields(String[] selectedFields) {
		this.selectedFields = selectedFields;
	}

    public String[] getSelectedFields() {
		return selectedFields;
	}

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingName(String mailingName) {
        this.mailingName = mailingName;
    }

    public String getMailingName() {
        return mailingName;
    }

	/**
	 * if overview = true, we have the recipient overview.
	 * if overview = false, we have the recipient search.
	 * @return
	 */
	public boolean isOverview() {
		return overview;
	}

	public void setOverview(boolean overview) {
		this.overview = overview;
	}

	public void setMessages(ActionMessages messages) {
		this.messages = messages;
	}

	public ActionMessages getMessages() {
		return this.messages;
	}

    public ActionMessages getErrors() {
        return errors;
    }

    public void addErrors(ActionMessages newErrors) {
        if (this.errors == null) {
            this.errors = new ActionErrors();
        }
        this.errors.add(newErrors);
    }

    public void resetErrors() {
        this.errors = new ActionErrors();
    }

    public void clearRecipientData() {
    	recipientID = 0;
        gender = 2;
        mailtype = 1;
    	title = "";
        firstname = "";
        lastname = "";
        email = "";
        column = new CaseInsensitiveMap<>();
        mailing = new HashMap<>();
    }

    public RecipientFrequencyCounterDto getFrequencyCounterDto() {
        return frequencyCounterDto;
    }

    public void setFrequencyCounterDto(RecipientFrequencyCounterDto frequencyCounterDto) {
        this.frequencyCounterDto = frequencyCounterDto;
    }


    @Deprecated
    public boolean checkParenthesisBalance() {
        return targetEqlBuilder.checkParenthesisBalance();
    }

    @Deprecated
	public void removeRule(int index) {
        targetEqlBuilder.removeRule(index);
	}

	@Deprecated
	public int getNumTargetNodes() {
		return targetEqlBuilder.getNumTargetNodes();
	}

	@Deprecated
	public String getColumnAndType(int index) {
		return targetEqlBuilder.getColumnAndType(index);
	}

	@Deprecated
	public void setColumnAndType(int index, String value) {
		targetEqlBuilder.setColumnAndType(index, value);
	}

	@Deprecated
	public int getChainOperator(int index) {
		return targetEqlBuilder.getChainOperator(index);
	}

	@Deprecated
	public void setChainOperator(int index, int value) {
		targetEqlBuilder.setChainOperator(index, value);
	}

	@Deprecated
	public int getParenthesisOpened(int index) {
		return targetEqlBuilder.getParenthesisOpened(index);
	}

	@Deprecated
	public void setParenthesisOpened(int index, int value) {
		targetEqlBuilder.setParenthesisOpened(index, value);
	}

	@Deprecated
	public int getPrimaryOperator(int index) {
		return targetEqlBuilder.getPrimaryOperator(index);
	}

	@Deprecated
	public void setPrimaryOperator(int index, int value) {
		targetEqlBuilder.setPrimaryOperator(index, value);
	}

	@Deprecated
	public String getPrimaryValue(int index) {
		return targetEqlBuilder.getPrimaryValue(index);
	}

	@Deprecated
	public void setPrimaryValue(int index, String value) {
		targetEqlBuilder.setPrimaryValue(index, value);
	}

	@Deprecated
	public int getParenthesisClosed(int index) {
		return targetEqlBuilder.getParenthesisClosed(index);
	}

	@Deprecated
	public void setParenthesisClosed(int index, int value) {
		targetEqlBuilder.setParenthesisClosed(index, value);
	}

	@Deprecated
	public String getDateFormat(int index) {
		return targetEqlBuilder.getDateFormat(index);
	}

	@Deprecated
	public void setDateFormat(int index, String value) {
		targetEqlBuilder.setDateFormat(index, value);
	}

	@Deprecated
	public int getSecondaryOperator(int index) {
		return targetEqlBuilder.getSecondaryOperator(index);
	}

	@Deprecated
	public void setSecondaryOperator(int index, int value) {
		targetEqlBuilder.setSecondaryOperator(index, value);
	}

	@Deprecated
	public String getSecondaryValue(int index) {
		return targetEqlBuilder.getSecondaryValue(index);
	}

	@Deprecated
	public void setSecondaryValue(int index, String value) {
		targetEqlBuilder.setSecondaryValue(index, value);
	}

	@Deprecated
	public List<String> getAllColumnsAndTypes() {
		return targetEqlBuilder.getAllColumnsAndTypes();
	}

	@Deprecated
	public void setValidTargetOperators(int index, ConditionalOperator[] operators) {
		targetEqlBuilder.setValidTargetOperators(index, operators);
	}

	@Deprecated
	public ConditionalOperator[] getValidTargetOperators(int index) {
		return targetEqlBuilder.getValidTargetOperators(index);
	}

	@Deprecated
    public void clearNewAdvancedSearch() {
        targetEqlBuilder.setColumnAndTypeNew("");
        targetEqlBuilder.setChainOperatorNew(0);
        targetEqlBuilder.setParenthesisOpenedNew(0);
        targetEqlBuilder.setPrimaryOperatorNew(0);
        targetEqlBuilder.setPrimaryValueNew("");
        targetEqlBuilder.setParenthesisClosedNew(0);
        targetEqlBuilder.setDateFormatNew("");
        targetEqlBuilder.setSecondaryOperatorNew(0);
        targetEqlBuilder.setSecondaryValueNew("");
    }

    public boolean isNeedSaveTargetGroup() {
        return needSaveTargetGroup;
    }

    public void setNeedSaveTargetGroup(boolean needSaveTargetGroup) {
        this.needSaveTargetGroup = needSaveTargetGroup;
    }

    public  String getTargetShortname() {
        return targetEqlBuilder.getShortname();
    }

    public void setTargetShortname(String targetShortname) {
        targetEqlBuilder.setShortname(targetShortname);
    }

    public String getTargetDescription() {
        return targetEqlBuilder.getDescription();
    }

    public void setTargetDescription(String targetDescription) {
        targetEqlBuilder.setDescription(targetDescription);
    }

	public final void setAdvancedSearch(final boolean value) {
		this.advancedSearch = value;
	}

	public final boolean isAdvancedSearch() {
		return this.advancedSearch;
	}

	@Deprecated
    public boolean isChangeToAdvancedSearch() {
        return changeToAdvancedSearch;
    }

    @Deprecated
    public void setChangeToAdvancedSearch(boolean changeToAdvancedSearch) {
        this.changeToAdvancedSearch = changeToAdvancedSearch;
    }


    public RecipientSearchParams generateSearchParams(){
        final RecipientSearchParams result = new RecipientSearchParams();
        result.setMailingListId(getListID());
        result.setTargetGroupId(getTargetID());
        result.setUserStatus(getUser_status());
        result.setUserType(getUser_type());
        result.setFirstName(getSearchFirstName());
        result.setLastName(getSearchLastName());
        result.setEmail(getSearchEmail());
        result.setQueryBuilderRules(getQueryBuilderRules());
        return result;
    }

    public void restoreSearchParams(final RecipientSearchParams searchParams) {
        setListID(searchParams.getMailingListId());
        setTargetID(searchParams.getTargetGroupId());
        setUser_status(searchParams.getUserStatus());
        setUser_type(searchParams.getUserType());
        setSearchFirstName(searchParams.getFirstName());
        setSearchLastName(searchParams.getLastName());
        setSearchEmail(searchParams.getEmail());
        setQueryBuilderRules(searchParams.getQueryBuilderRules());
        if (isAdvancedSearch()) {
            setChangeToAdvancedSearch(true);
        }
    }
    /**
     * Get index of conditional field
     *
     * @param field
     * @return found index of field column otherwise return -1
     */
    @Deprecated
    public int findConditionalIndex(String field) {
        return targetEqlBuilder.findConditionalIndex(field);
    }

    @Deprecated
    //TODO: GWUA-4678: delete after migrate successfully; implemented by JS
    public void cleanRulesForBasicSearch() {
        if (StringUtils.isNotBlank(getSearchFirstName()) ||
                StringUtils.isNotBlank(getSearchLastName()) ||
                StringUtils.isNotBlank(getSearchEmail())) {
			for (String field: Arrays.asList("FIRSTNAME", "LASTNAME", "EMAIL")) {
                final int conditionalIndex = findConditionalIndex(field);
                if (conditionalIndex != -1) {
                    removeRule(conditionalIndex);
                }
            }
		}
    }

    @Deprecated
    //TODO: GWUA-4678: delete after migrate successfully; implemented by JS
    public int createRuleFromBasicSearch(int lastIndex, String field, String value) {
        final int conditionalIndex = findConditionalIndex(field);
        if (conditionalIndex != -1) {
            removeRule(conditionalIndex);
            return --lastIndex;
        }

        if (StringUtils.isNotBlank(value)) {
            addSimpleRule(lastIndex, field, ConditionalOperator.LIKE, value);
            return ++lastIndex;
        }

        return lastIndex;
    }

    @Deprecated
    //TODO: GWUA-4678: delete after migrate successfully; implemented by JS
    public void addSimpleRule(int lastIndex, String field, ConditionalOperator operator, String value) {
        targetEqlBuilder.setColumnAndType(lastIndex, field);
        targetEqlBuilder.setChainOperator(lastIndex, targetEqlBuilder.getChainOperatorNew());
        targetEqlBuilder.setParenthesisOpened(lastIndex, targetEqlBuilder.getParenthesisOpenedNew());
        targetEqlBuilder.setPrimaryOperator(lastIndex, operator.getOperatorCode());
        targetEqlBuilder.setPrimaryValue(lastIndex, StringUtils.trim(value));
        targetEqlBuilder.setParenthesisClosed(lastIndex, targetEqlBuilder.getParenthesisClosedNew());
        targetEqlBuilder.setDateFormat(lastIndex, targetEqlBuilder.getDateFormatNew());
        targetEqlBuilder.setSecondaryOperator(lastIndex, targetEqlBuilder.getSecondaryOperatorNew());
        targetEqlBuilder.setSecondaryValue(lastIndex, targetEqlBuilder.getSecondaryValueNew());
    }

    @Deprecated
    //TODO: GWUA-4678: delete after migrate successfully; implemented by JS
    public void updateBasicSearchFromRules() {
        for (String field: Arrays.asList("FIRSTNAME", "LASTNAME", "EMAIL")) {
            final int conditionalIndex = findConditionalIndex(field);
            if (conditionalIndex != -1) {
                String value = targetEqlBuilder.getPrimaryValue(conditionalIndex);
                switch (field) {
                    case "FIRSTNAME":
                        setSearchFirstName(value);
                        break;
                    case "LASTNAME":
                        setSearchLastName(value);
                        break;
                    case "EMAIL":
                        setSearchEmail(value);
                        break;
                    default:
                        //nothing do
                }
            }
        }
    }

    @Deprecated
    public TargetEqlBuilder getTargetEqlBuilder() {
        return targetEqlBuilder;
    }

    @Deprecated
    public void setTargetEqlBuilder(TargetEqlBuilder targetEqlBuilder) {
        this.targetEqlBuilder = targetEqlBuilder;
    }

    @Deprecated
    public void setColumnName(int index, String value) {
		targetEqlBuilder.setColumnName(index, value);
	}

	@Deprecated
	public String getColumnName(int index) {
		return targetEqlBuilder.getColumnName(index);
	}

	@Deprecated
	public void setColumnType(int index, int type) {
		targetEqlBuilder.setColumnType(index, type);
	}

	@Deprecated
	public int getColumnType(int index) {
		return targetEqlBuilder.getColumnType(index);
	}

	@Deprecated
	public String getColumnAndTypeNew() {
		return targetEqlBuilder.getColumnAndTypeNew();
	}

	@Deprecated
	public void setColumnAndTypeNew(String columnAndTypeNew) {
		targetEqlBuilder.setColumnAndTypeNew(columnAndTypeNew);
	}

	@Deprecated
	public int getChainOperatorNew() {
		return targetEqlBuilder.getChainOperatorNew();
	}

	@Deprecated
	public void setChainOperatorNew(int chainOperatorNew) {
		targetEqlBuilder.setChainOperatorNew(chainOperatorNew);
	}

	@Deprecated
	public int getParenthesisOpenedNew() {
		return targetEqlBuilder.getParenthesisOpenedNew();
	}

	@Deprecated
	public void setParenthesisOpenedNew(int parenthesisOpenedNew) {
		targetEqlBuilder.setParenthesisOpenedNew(parenthesisOpenedNew);
	}

	@Deprecated
	public int getPrimaryOperatorNew() {
		return targetEqlBuilder.getPrimaryOperatorNew();
	}

	@Deprecated
	public void setPrimaryOperatorNew(int primaryOperatorNew) {
		targetEqlBuilder.setPrimaryOperatorNew(primaryOperatorNew);
	}

	@Deprecated
	public String getPrimaryValueNew() {
		return targetEqlBuilder.getPrimaryValueNew();
	}

	@Deprecated
	public void setPrimaryValueNew(String primaryValueNew) {
		targetEqlBuilder.setPrimaryValueNew(primaryValueNew);
	}

	@Deprecated
	public int getParenthesisClosedNew() {
		return targetEqlBuilder.getParenthesisClosedNew();
	}

	@Deprecated
	public void setParenthesisClosedNew(int parenthesisClosedNew) {
		targetEqlBuilder.setParenthesisClosedNew(parenthesisClosedNew);
	}

	@Deprecated
	public String getDateFormatNew() {
		return targetEqlBuilder.getDateFormatNew();
	}

	@Deprecated
	public void setDateFormatNew(String dateFormatNew) {
		targetEqlBuilder.setDateFormatNew(dateFormatNew);
	}

	@Deprecated
	public int getSecondaryOperatorNew() {
		return targetEqlBuilder.getSecondaryOperatorNew();
	}

	@Deprecated
	public void setSecondaryOperatorNew(int secondaryOperatorNew) {
		targetEqlBuilder.setSecondaryOperatorNew(secondaryOperatorNew);
	}

	@Deprecated
	public String getSecondaryValueNew() {
		return targetEqlBuilder.getSecondaryValueNew();
	}

	@Deprecated
	public void setSecondaryValueNew(String secondaryValueNew) {
		targetEqlBuilder.setSecondaryValueNew(secondaryValueNew);
	}

	@Deprecated
    public Date getCurrentDate(){
        return new Date();
    }

    @Deprecated
    public Date getYearAgoDate(){
        return DateUtils.addYears(new Date(), -1);
    }

    public String getQueryBuilderRules() {
        return queryBuilderRules;
    }

    public void setQueryBuilderRules(String queryBuilderRules) {
        this.queryBuilderRules = queryBuilderRules;
    }

	public int getLatestDataSourceId() {
		return latestDataSourceId;
	}

	public void setLatestDataSourceId(int latestDataSourceId) {
		this.latestDataSourceId = latestDataSourceId;
	}
}
