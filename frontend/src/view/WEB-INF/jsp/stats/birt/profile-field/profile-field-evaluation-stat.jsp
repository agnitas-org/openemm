<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="profileFieldStatForm" type="com.agnitas.emm.core.birtstatistics.profiledb.form.ProfileFieldStatForm"--%>
<%--@elvariable id="profileFields" type="com.agnitas.emm.core.service.RecipientFieldDescription"--%>
<%--@elvariable id="targets" type="java.util.List"--%>
<%--@elvariable id="mailinglists" type="java.util.List"--%>
<%--@elvariable id="statUrl" type="java.lang.String"--%>

<mvc:form servletRelativeAction="/profiledb/statistic.action" method="GET" modelAttribute="profileFieldStatForm" data-form="resource">
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="report.mailing.filter"/></h2>
            <ul class="tile-header-actions">
                <li>
                    <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                        <i class="icon icon-refresh"></i>
                        <span class="text"><mvc:message code="button.Show"/></span>
                    </button>
                </li>
            </ul>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="profile-field"><mvc:message code="workflow.start.ProfileField"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select id="profile-field" path="colName" cssClass="form-control js-select">
                        <mvc:options items="${profileFields}" itemLabel="shortName" itemValue="columnName" />
                    </mvc:select>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="targetId"><mvc:message code="Target"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="targetId" id="targetId" cssClass="form-control js-select">
                        <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                        <mvc:options items="${targets}" itemValue="id" itemLabel="targetName"/>
                    </mvc:select>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="mailingListId"><mvc:message code="Mailinglist"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="mailingListId" id="mailingListId" cssClass="form-control js-select">
                        <mvc:option value="0"><mvc:message code="statistic.All_Mailinglists"/></mvc:option>
                        <mvc:options items="${mailinglists}" itemValue="id" itemLabel="shortname"/>
                    </mvc:select>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="limit"><mvc:message code="statistic.profile.values.max"/></label>
                </div>
                <div class="col-sm-8">
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
    </div>

    <div class="tile">
        <div class="tile-header">
            <div class="headline"><mvc:message code="statistic.profile"/></div>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-cloud-download"></i>
                        <span class="text"><mvc:message code="Export"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="statistics.exportFormat"/></li>
                        <li>
                            <a href="${statUrl}&__format=csv" tabindex="-1" data-prevent-load="">
                                <i class="icon icon-file-excel-o"></i> <mvc:message code='export.message.csv'/>
                            </a>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content">
            <iframe src="${statUrl}&__format=html" border="0" scrolling="auto" width="100%" height="100px" frameborder="0">
                Your Browser does not support IFRAMEs, please update!
            </iframe>
        </div>
    </div>
</mvc:form>
