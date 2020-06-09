<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.agnitas.dao.UserStatus"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.agnitas.emm.core.Permission"%>
<%@ page language="java"
         import="com.agnitas.emm.core.mediatypes.common.MediaTypes, com.agnitas.web.ComRecipientForm, org.agnitas.beans.BindingEntry, org.agnitas.beans.Mailinglist, org.agnitas.beans.Recipient, org.agnitas.util.AgnUtils, org.agnitas.web.RecipientAction, org.springframework.context.ApplicationContext, org.springframework.web.context.support.WebApplicationContextUtils"
         contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.agnitas.beans.ProfileField" %>
<%@ page import="org.agnitas.util.DateUtilities" %>
<%@ page import="com.agnitas.web.ComRecipientAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="recipientForm" type="org.agnitas.web.RecipientForm"--%>
<%--@elvariable id="isRecipientEmailInUseWarningEnabled" type="java.lang.Boolean"--%>

<c:set var="allowedValueDatePattern" value="<%= new SimpleDateFormat(DateUtilities.DD_MM_YYYY).toPattern() %>"/>
<c:set var="columnValueDatePattern" value="<%= new SimpleDateFormat(DateUtilities.ISO_8601_DATETIME_FORMAT).toPattern() %>"/>

<%
    ApplicationContext aContext = WebApplicationContextUtils.getWebApplicationContext(application);
    ComRecipientForm recipient = (ComRecipientForm) session.getAttribute("recipientForm");
    Recipient cust = (Recipient) aContext.getBean("Recipient");

    if (recipient == null) {
        recipient = new ComRecipientForm();
    }
%>
<c:set var="MAILTYPE_TEXT" value="<%= Recipient.MAILTYPE_TEXT%>" scope="page"/>
<c:set var="MAILTYPE_HTML" value="<%= Recipient.MAILTYPE_HTML%>" scope="page"/>
<c:set var="MAILTYPE_HTML_OFFLINE" value="<%= Recipient.MAILTYPE_HTML_OFFLINE%>" scope="page"/>

<c:set var="MODE_EDIT_EDITABLE" value="<%= ProfileField.MODE_EDIT_EDITABLE %>"/>
<c:set var="MODE_EDIT_READONLY" value="<%= ProfileField.MODE_EDIT_READONLY %>"/>
<c:set var="MODE_EDIT_NOT_VISIBLE" value="<%= ProfileField.MODE_EDIT_NOT_VISIBLE %>"/>

<c:set var="ACTION_CONFIRM_DELETE" value="<%= RecipientAction.ACTION_CONFIRM_DELETE %>"     scope="page" />
<c:set var="ACTION_CHECK_ADDRESS" value="<%= ComRecipientAction.ACTION_CHECK_ADDRESS %>"    scope="page" />
<c:set var="ACTION_VIEW" value="<%= ComRecipientAction.ACTION_VIEW %>"                      scope="page" />

<c:choose>
    <c:when test="${recipientForm.recipientID == 0}">
        <c:set var="headline" scope="page">
            <bean:message key="recipient.NewRecipient"/>
        </c:set>
    </c:when>
    <c:otherwise>
        <c:set var="headline" scope="page">
            <bean:message key="recipient.RecipientEdit"/>
        </c:set>
    </c:otherwise>
</c:choose>

<script type="text/javascript">
    $(document).ready(function() {
        _.each($('.js-show-tile-if-checked'), function(trigger) {
            var $trigger    = $(trigger),
                $target     = $(document).find($trigger.data('toggle-tile')),
                $checkboxes = $target.find('input[type="checkbox"]:checked');

            if ($checkboxes.length) {
                AGN.Lib.Tile.show($trigger);
            }
        })
    });
</script>

<agn:agnForm action="/recipient" id="recipientForm" data-form="resource" data-controller="recipient-view" data-action="recipient-save">
    <html:hidden property="action"/>
    <html:hidden property="recipientID"/>
    <html:hidden property="user_type"/>
    <html:hidden property="user_status"/>
    <html:hidden property="listID"/>
    <html:hidden property="targetID"/>

    <c:url var="checkAddressLink" value="/recipient.do">
        <c:param name="action" value="${ACTION_CHECK_ADDRESS}"/>
        <c:param name="recipientID" value="${recipientForm.recipientID}"/>
    </c:url>

    <c:url var="userViewLinkPattern" value="/recipient.do?recipientID={recipient-ID}">
        <c:param name="action" value="${ACTION_VIEW}"/>
    </c:url>

    <c:if test="${isRecipientEmailInUseWarningEnabled}">
        <script type="application/json" data-initializer="recipient-view">
            {
              "urls": {
                "CHECK_ADDRESS": "${checkAddressLink}",
                "EXISTING_USER_URL_PATTERN": "${userViewLinkPattern}"
              }
            }
        </script>
    </c:if>

    <div class="row">
        <div class="col-md-6">
            <div class="tile">
                <div class="tile-header">
                    <h2 class="headline">${headline}</h2>
                </div>
                <div class="tile-content">
                    <div class="tile-content-forms">

                        <div class="form-group">
                            <div class="col-sm-4">
                              <label for="recipient-salutation" class="control-label">
                                  <bean:message key="recipient.Salutation"/>
                              </label>
                            </div>
                            <div class="col-sm-8">
                                <html:select styleId="recipient-salutation" property="gender" styleClass="js-select form-control">
                                    <html:option value="0"><bean:message key="recipient.gender.0.short"/></html:option>
                                    <html:option value="1"><bean:message key="recipient.gender.1.short"/></html:option>
                                    <emm:ShowByPermission token="recipient.gender.extended">
                                        <html:option value="3"><bean:message key="recipient.gender.3.short"/></html:option>
                                        <html:option value="4"><bean:message key="recipient.gender.4.short"/></html:option>
                                        <html:option value="5"><bean:message key="recipient.gender.5.short"/></html:option>
                                    </emm:ShowByPermission>
                                    <html:option value="2"><bean:message key="recipient.gender.2.short"/></html:option>
                                </html:select>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-4">
                              <label for="recipient-title" class="control-label">
                                  <bean:message key="Title"/>
                              </label>
                            </div>
                            <div class="col-sm-8">
                                <html:text styleId="recipient-title" styleClass="form-control" property="title"/>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-4">
                              <label for="recipient-firstname" class="control-label">
                                  <bean:message key="Firstname"/>
                              </label>
                            </div>
                            <div class="col-sm-8">
                                <html:text styleId="recipient-firstname" styleClass="form-control" property="firstname"/>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-4">
                              <label for="recipient-lastname" class="control-label">
                                  <bean:message key="Lastname"/>
                              </label>
                            </div>
                            <div class="col-sm-8">
                                <html:text styleId="recipient-lastname" styleClass="form-control" property="lastname"/>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-4">
                              <label for="recipient-email" class="control-label">
                                  <bean:message key="mailing.MediaType.0"/>
                              </label>
                            </div>
                            <div class="col-sm-8">
                                <agn:agnText styleId="recipient-email" styleClass="form-control" property="email"/>
                            </div>
                        </div>

                        <emm:ShowByPermission token="recipient.tracking.veto">
						<div class="form-group">
                            <div class="col-sm-4">
                              <label for="trackingVeto" class="control-label">
                                  <bean:message key="recipient.trackingVeto"/>
                              </label>
                            </div>
                            <div class="col-sm-8">
                				<html:hidden property="__STRUTS_CHECKBOX_trackingVeto" value="false"/>
  								<label class="toggle">
  									<html:checkbox property="trackingVeto" styleId="trackingVeto"/>
                          			<div class="toggle-control"></div>
  								</label>
            				</div>
                        </div>
                        </emm:ShowByPermission>
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label">
                                    <bean:message key="Mailtype"/>
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <ul class="list-group">
                                    <li class="list-group-item">
                                        <label class="radio-inline">
                                            <html:radio property="mailtype" value="${MAILTYPE_TEXT}"/>
                                            <bean:message key="MailType.0"/>
                                        </label>
                                    </li>
                                    <li class="list-group-item">
                                        <label class="radio-inline">
                                            <html:radio property="mailtype" value="${MAILTYPE_HTML}"/>
                                            <bean:message key="MailType.1"/>
                                        </label>
                                    </li>
                                    <li class="list-group-item">
                                        <label class="radio-inline">
                                            <html:radio property="mailtype" value="${MAILTYPE_HTML_OFFLINE}"/>
                                            <bean:message key="MailType.2"/>
                                        </label>
                                    </li>
                                </ul>
                            </div>
                        </div>

                        <%@include file="recipient-freq-counter.jspf" %>

	                </div>
                </div>
            </div>


            <div class="tile">
                <div class="tile-header">
                    <h2 class="headline"><bean:message key="recipient.More_Profile_Data"/></h2>
                </div>
                <div class="tile-content">
                    <div class="tile-content-forms">
                        <emm:ShowColumnInfo id="agnTbl" table="<%= AgnUtils.getCompanyID(request) %>" useCustomSorting="true"
                            hide="email, title, gender, mailtype, firstname, lastname, change_date, bounceload, facebook_status, foursquare_status, google_status, xing_status, twitter_status, sys_tracking_veto, cleaned_date, facebook_status, foursquare_status, google_status, twitter_status, xing_status, freq_count_day, freq_count_week, freq_count_month">

                            <%--@elvariable id="_agnTbl_data_type" type="java.lang.String"--%>
                            <%--@elvariable id="_agnTbl_column_name" type="java.lang.String"--%>
                            <%--@elvariable id="_agnTbl_column" type="java.lang.String"--%>
                            <%--@elvariable id="_agnTbl_data_length" type="java.lang.Integer"--%>
                            <%--@elvariable id="_agnTbl_shortname" type="java.lang.String"--%>
                            <%--@elvariable id="_agnTbl_editable" type="java.lang.Integer"--%>
                            <%--@elvariable id="_agnTbl_line" type="java.lang.Integer"--%>
                            <%--@elvariable id="_agnTbl_allowed_values" type="java.lang.String[]"--%>
                            <%--@elvariable id="_agnTbl_nullable" type="java.lang.Integer"--%>

                            <c:set var="column_name" value="${_agnTbl_column_name}"/>
                            <c:if test="${empty column_name}">
                                <c:set var="column_name" value="${_agnTbl_column}"/>
                            </c:if>

                            <c:set var="propName" value="column(${column_name})"/>

                            <c:choose>
                                <c:when test="${_agnTbl_editable == MODE_EDIT_EDITABLE}">
                                    <c:set var="isReadable" value="true"/>
                                    <c:set var="isWritable" value="true"/>
                                </c:when>
                                <c:when test="${_agnTbl_editable == MODE_EDIT_READONLY}">
                                    <c:set var="isReadable" value="true"/>
                                    <c:set var="isWritable" value="false"/>
                                </c:when>
                                <c:when test="${_agnTbl_editable == MODE_EDIT_NOT_VISIBLE}">
                                    <c:set var="isReadable" value="false"/>
                                    <c:set var="isWritable" value="false"/>
                                </c:when>
                            </c:choose>

                            <c:if test="${isReadable}">
                                <c:choose>
                                    <c:when test="${_agnTbl_data_type == 'DATE'}">
                                        <div class="form-group">
                                            <div class="col-sm-4">
                                                <label class="control-label multiline-auto">${_agnTbl_shortname}:</label>
                                            </div>
                                            <c:choose>
                                                <c:when test="${not empty _agnTbl_allowed_values and isWritable}">
                                                    <div class="col-sm-8" data-field="date-split">
                                                        <c:set var="selectedDateIso"><bean:write name="recipientForm" property="${propName}"/></c:set>
                                                        <fmt:parseDate var="selectedDate" value="${selectedDateIso}" pattern="${columnValueDatePattern}"/>
                                                        <c:set var="selectedDateMilliseconds" value="${selectedDate.time}"/>

                                                        <agn:agnSelect property="${propName}" value="${selectedDateMilliseconds}" styleClass="form-control js-select" data-field-date-split="">
                                                            <c:if test="${_agnTbl_nullable == 1}">
                                                                <html:option value="">NULL</html:option>
                                                            </c:if>

                                                            <c:set var="selectedDateIsAllowed" value="false"/>
                                                            <c:forEach var="allowedValue" items="${_agnTbl_allowed_values}">
                                                                <fmt:parseDate var="allowedDate" value="${allowedValue}" pattern="${allowedValueDatePattern}"/>
                                                                <c:set var="allowedDateMilliseconds" value="${allowedDate.time}"/>
                                                                <html:option value="${allowedDateMilliseconds}">${fn:escapeXml(allowedValue)}</html:option>

                                                                <c:if test="${selectedDateMilliseconds == allowedDateMilliseconds}">
                                                                    <c:set var="selectedDateIsAllowed" value="true"/>
                                                                </c:if>
                                                            </c:forEach>

                                                            <!-- If a selected (currently stored in DB) date value isn't present among allowed values -->
                                                            <c:if test="${not selectedDateIsAllowed}">
                                                                <html:option value="${selectedDateMilliseconds}"><fmt:formatDate value="${selectedDate}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}" /></html:option>
                                                            </c:if>
                                                        </agn:agnSelect>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
	                                                <div class="col-sm-8">
	                                               		<c:choose>
		                                                	<c:when test="${isWritable}">
		                                                		<c:set var="currentValue"><bean:write name="recipientForm" property="${propName}"/></c:set>
		                                                		<input name="${propName}" value="${currentValue}" class="form-control datepicker-input js-datepicker" data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}', formatSubmit: '${fn:toLowerCase(adminDateFormat)}'"/>
			                                                </c:when>
			                                                <c:otherwise>
			                                                	<html:text property="${propName}" styleClass="form-control" readonly="${not isWritable}" />
			                                                </c:otherwise>
	                                                	</c:choose>
													</div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="form-group">
                                            <div class="col-sm-4">
                                                <label class="control-label multiline-auto">${_agnTbl_shortname}:</label>
                                            </div>
                                            <div class="col-sm-8">
                                                <c:choose>
                                                    <c:when test="${not empty _agnTbl_allowed_values and isWritable}">
                                                        <c:set var="selectedValue"><bean:write name="recipientForm" property="${propName}"/></c:set>

                                                        <agn:agnSelect property="${propName}" styleClass="form-control js-select">
                                                            <c:if test="${_agnTbl_nullable == 1}">
                                                                <html:option value="">NULL</html:option>
                                                            </c:if>

                                                            <c:set var="selectedValueIsAllowed" value="false"/>
                                                            <c:forEach var="allowedValue" items="${_agnTbl_allowed_values}">
                                                                <html:option value="${fn:escapeXml(allowedValue)}">${fn:escapeXml(allowedValue)}</html:option>
                                                                <c:if test="${allowedValue == selectedValue}">
                                                                    <c:set var="selectedValueIsAllowed" value="true"/>
                                                                </c:if>
                                                            </c:forEach>

                                                            <!-- If a selected (currently stored in DB) value isn't present among allowed values -->
                                                            <c:if test="${not selectedValueIsAllowed}">
                                                                <html:option value="${fn:escapeXml(selectedValue)}">${fn:escapeXml(selectedValue)}</html:option>
                                                            </c:if>
                                                        </agn:agnSelect>
                                                    </c:when>
                                                    <c:when test="${_agnTbl_data_type == 'VARCHAR'}">
                                                        <html:text property="${propName}" styleClass="form-control" maxlength="${_agnTbl_data_length}" readonly="${not isWritable}"/>
                                                    </c:when>
                                                   <c:when test="${_agnTbl_column_name == 'DATASOURCE_ID' or _agnTbl_column_name == 'datasource_id'}">
                                                        <a href="importexport/datasource/list.action"><html:text property="${propName}" styleClass="form-control" readonly="${not isWritable}"/></a>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <html:text property="${propName}" styleClass="form-control" readonly="${not isWritable}"/>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <c:if test="${_agnTbl_line eq 1}">
                                    <div class="tile-separator"></div>
                                </c:if>
                            </c:if>
                        </emm:ShowColumnInfo>

                    </div>
                </div>
            </div>

        </div>

        <div class="col-md-6">
            <div class="tile">
                <div class="tile-header">
                    <h2 class="headline"><bean:message key="recipient.Mailinglists"/></h2>
                </div>

                <div class="tile-content tile-content-forms">

                    <%
                        BindingEntry tmpStatusEntry = null;
                        int tmpUserStatus;
                        String tmpUserType = null;
                        String tmpUserRemark = null;
                        java.util.Date tmpUserDate = null;
                        SimpleDateFormat aFormat = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY));
                        aFormat.applyPattern(aFormat.toPattern().replaceFirst("y+", "yyyy").replaceFirst(", ", " "));
                        boolean aMType = false;
                        int k = 0;

                        // just for debugging:
                        // please clean me up asap:
                        Map MTL = new HashMap();
                        Integer mi;

                        // for emm:ShowByPermission keys
                        String[] ES = {"email", "fax", "post", "mms", "sms"};

                        cust.setCompanyID(AgnUtils.getCompanyID(request));
                        cust.setCustomerID(recipient.getRecipientID());

                        Map allCustLists = cust.getAllMailingLists();
                        recipient.getAllBindings().clear();
                    %>
                    <c:forEach var="mailinglist" items="${mailinglists}">
                        <div class="tile">
                            <div class="tile-header">
                                <a href="#" class="headline js-show-tile-if-checked" data-toggle-tile="#tile-recipient-mailinglist-${mailinglist.id}">
                                    <i class="tile-toggle icon icon-angle-down"></i>
                                    ${mailinglist.shortname}
                                </a>
                            </div>
                            <div class="tile-content tile-content-forms hidden" id="tile-recipient-mailinglist-${mailinglist.id}">
                                <%
                                    mi = ((Mailinglist) pageContext.getAttribute("mailinglist")).getId();
                                    if (allCustLists.get(mi) != null) {
                                        MTL = (Map) (allCustLists.get(mi));
                                    } else {
                                        MTL = new HashMap();
                                        allCustLists.put(mi, MTL);
                                    }

                                    for (k = 0; k < ES.length; k++) {
                                        if (ES[k] == null) {
                                            continue;
                                        }
                                        tmpStatusEntry = ((BindingEntry) (MTL.get(new Integer(k))));
                                        if (tmpStatusEntry == null) {
                                            tmpStatusEntry = new org.agnitas.beans.impl.BindingEntryImpl();
                                            tmpStatusEntry.setCustomerID(recipient.getRecipientID());
                                            tmpStatusEntry.setMailinglistID(mi);
                                        }
                                        tmpUserType = tmpStatusEntry.getUserType();
                                        tmpUserStatus = tmpStatusEntry.getUserStatus();
                                        tmpUserRemark = tmpStatusEntry.getUserRemark() + (StringUtils.isNotBlank(tmpStatusEntry.getReferrer()) ? "Ref: " + AgnUtils.getMaxLengthString(tmpStatusEntry.getReferrer(), 15, "...") : "");
                                        tmpUserDate = tmpStatusEntry.getChangeDate();
                                        int mti = ((Mailinglist) pageContext.getAttribute("mailinglist")).getId();
                                        recipient.setBindingEntry(mti, tmpStatusEntry);
                                %>
                                <emm:ShowByPermission token='<%= "mediatype."+ES[k] %>'>
                                    <div class="form-group">
                                        <div class="col-sm-2"></div>
                                        <div class="col-sm-3 control-label-left">
                                            <label class="checkbox-inline control-label">
                                                <html:checkbox property='<%= ES[k]+"Entry["+mti+"].userStatus" %>' value="1"/>
                                                <bean:message key='<%= "mailing.MediaType." + k %>'/>
                                                <input type="hidden"
                                                       name='<%= "__STRUTS_CHECKBOX_"+ES[k]+"Entry["+mti+"].userStatus" %>'
                                                       value="<%= ((tmpUserStatus == UserStatus.Active.getStatusCode())?3:tmpUserStatus) %>">
                                            </label>
                                        </div>
                                        <div class="col-sm-7">
                                            <html:select styleClass="form-control js-warn-on-change" property='<%= ES[k] + "Entry[" + mti + "].userType" %>'>
                                                <html:option value="A"><bean:message key="recipient.Administrator"/></html:option>
                                                <html:option value="T"><bean:message key="TestSubscriber"/></html:option>
                                                <%@include file="recipient-novip-test.jspf" %>
                                                <html:option value="W"><bean:message key="NormalSubscriber"/></html:option>
                                                <%@include file="recipient-novip-normal.jspf" %>
                                            </html:select>
                                        </div>

                                    </div>

                                    <% if (tmpUserStatus > 0 && tmpUserStatus <= 7) { %>
                                        <div class="form-group">
                                            <div class="col-sm-2"></div>
                                            <div class="col-sm-3 control-label-left">
                                                <label class="control-label">
                                                    <bean:message key="recipient.Status"/>
                                                </label>
                                            </div>
                                            <div class="col-sm-7">
                                                <p class="form-control-static"><bean:message key='<%= "recipient.MailingState"+tmpUserStatus %>'/></p>
                                            </div>
                                        </div>
                                    <% } %>

                                    <% if (tmpUserRemark.length() > 0) { %>
                                        <div class="form-group">
                                            <div class="col-sm-2"></div>
                                            <div class="col-sm-3 control-label-left">
                                                <label class="control-label">
                                                    <bean:message key="recipient.Remark"/>
                                                </label>
                                            </div>
                                            <div class="col-sm-7">
                                                <p class="form-control-static">
                                                    <%= tmpUserRemark %>
                                                </p>

                                                <% if (tmpUserDate != null) { %>
                                                    <p class="form-control-static">
                                                        <%= aFormat.format(tmpUserDate) %>
                                                    </p>
                                                <% } %>
                                            </div>

                                        </div>
                                    <% } %>


									<%
										boolean hasMediatypePermission = false;
										if(k < ES.length - 1) {
                                            for (int nextMediatype = k + 1; nextMediatype < ES.length; nextMediatype++) {
                                            	for(final MediaTypes mediaType : MediaTypes.values()) {
                                            		if(mediaType.toString().toLowerCase().equals(ES[nextMediatype]) && AgnUtils.allowed(request, mediaType.getRequiredPermission())) {
													hasMediatypePermission = true;
														break;
                                            		}
                                            	}
                                            }
										}
										
										if(hasMediatypePermission) {
		  									%>													
                                            <div class="tile-separator"></div>

											<%                   
										}
									%>

                                    

                                </emm:ShowByPermission>
                                <%
                                    }
                                %>

                            </div>
                        </div>

                    </c:forEach>

                </div>

            </div>
        </div>
    </div>

    <script id="email-confirmation-modal" type="text/x-mustache-template">
        <div class="modal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i></button>
                        <h4 class="modal-title">
                            <bean:message key="warning"/>
                        </h4>
                    </div>

                    <div class="modal-body">
                        <p>{{- question }}</p>
                    </div>

                    <div class="modal-footer">
                        <div class="btn-group">
                            <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                                <i class="icon icon-times"></i>
                                <span class="text">
                                    <bean:message key="button.Cancel"/>
                                </span>
                            </button>

                            <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                                <i class="icon icon-check"></i>
                                <span class="text">
                                    <bean:message key="button.Proceed"/>
                                </span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </script>

</agn:agnForm>
