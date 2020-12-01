<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--@elvariable id="recipientForm" type="com.agnitas.web.ComRecipientForm"--%>

<html:form action="/recipient">
    <html:hidden property="action"/>
    <html:hidden property="numberOfRowsChanged"/>
    <html:hidden property="recipientID"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "recipient-status-history-overview": {
                "rows-count": ${recipientForm.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="default.search"/>
            </h2>
            <ul class="tile-header-nav">
            </ul>

            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><bean:message key="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><bean:message key="listSize"/></li>
                        <li>
                            <label class="label">
                                <html:radio property="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
                                </button>
                            </p>
                            <logic:iterate collection="${recipientForm.columnwidthsList}" indexId="i" id="width">
                                <html:hidden property="columnwidthsList[${i}]"/>
                            </logic:iterate>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content">

            <div class="table-wrapper">
                <display:table
                        class="table table-bordered js-table"
                        id="historyEntry"
                        name="recipientHistory"
                        pagesize="${recipientForm.numberOfRows}"
                        requestURI="/recipient.do?action=${ACTION_HISTORY_VIEW}&recipientID=${recipientForm.recipientID}"
                        excludedParams="*"
                        decorator="com.agnitas.util.RecipientsTableDecorator">

                    <display:column headerClass="js-table-sort" titleKey="Date" sortable="true" format="{0,date,${localeTablePattern}}" property="changeDate" />

                    <display:column headerClass="js-table-sort" titleKey="recipient.history.fieldname" sortable="true" property="fieldName" />

                    <display:column headerClass="js-table-sort" titleKey="recipient.history.oldvalue" sortable="true" property="oldValue" />

                    <display:column headerClass="js-table-sort" titleKey="recipient.history.newvalue" sortable="true" property="newValue" />

                </display:table>
            </div>

        </div>
    </div>
</html:form>
