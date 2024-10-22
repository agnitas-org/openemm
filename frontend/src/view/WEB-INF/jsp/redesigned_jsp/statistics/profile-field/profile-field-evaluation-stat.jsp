<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="profileFieldStatForm" type="com.agnitas.emm.core.birtstatistics.profiledb.form.ProfileFieldStatForm"--%>
<%--@elvariable id="profileFields" type="com.agnitas.emm.core.service.RecipientFieldDescription"--%>
<%--@elvariable id="targets" type="java.util.List"--%>
<%--@elvariable id="mailinglists" type="java.util.List"--%>
<%--@elvariable id="statUrl" type="java.lang.String"--%>

<mvc:form cssClass="tiles-container flex-column" id="stat-form" servletRelativeAction="/profiledb/statistic.action"
          method="GET" modelAttribute="profileFieldStatForm" data-editable-view="${agnEditViewKey}">
    <div id="filter-tile" class="tile h-auto flex-none" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="report.mailing.filter" /></h1>
        </div>
        <div class="tile-body form-column-4">
            <div>
                <label class="form-label" for="profile-field"><mvc:message code="workflow.start.ProfileField"/></label>
                <mvc:select id="profile-field" path="colName" cssClass="form-control js-select">
                    <mvc:options items="${profileFields}" itemLabel="shortName" itemValue="columnName" />
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="targetId"><mvc:message code="Target"/></label>
                <mvc:select path="targetId" id="targetId" cssClass="form-control js-select">
                    <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                    <mvc:options items="${targets}" itemValue="id" itemLabel="targetName"/>
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="mailingListId"><mvc:message code="Mailinglist"/></label>
                <mvc:select path="mailingListId" id="mailingListId" cssClass="form-control js-select">
                    <mvc:option value="0"><mvc:message code="statistic.All_Mailinglists"/></mvc:option>
                    <mvc:options items="${mailinglists}" itemValue="id" itemLabel="shortname"/>
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="limit"><mvc:message code="statistic.profile.values.max"/></label>
                <mvc:select path="limit" id="limit" cssClass="form-control js-select">
                    <mvc:option value="5" label="5"/>
                    <mvc:option value="10" label="10"/>
                    <mvc:option value="15" label="15"/>
                    <mvc:option value="20" label="20"/>
                    <mvc:option value="25" label="25"/>
                </mvc:select>
            </div>
        </div>
    </div>

    <div id="stat-tile" class="tile" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="statistic.profile"/></h1>
        </div>
        <div class="tile-body p-2 js-scrollable" style="overflow-y: auto !important;">
            <iframe src="${statUrl}&__format=html" width="100%" height="100px">
                Your Browser does not support IFRAMEs, please update!
            </iframe>
        </div>
    </div>
</mvc:form>
