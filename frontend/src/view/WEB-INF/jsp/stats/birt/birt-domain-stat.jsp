<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="domainStatisticForm" type="com.agnitas.emm.core.birtstatistics.domain.form.DomainStatisticForm"--%>
<%--@elvariable id="targetList" type="java.util.List"--%>
<%--@elvariable id="mailingLists" type="java.util.List"--%>
<%--@elvariable id="birtStatisticUrlWithoutFormat" type="java.lang.String"--%>

<mvc:form servletRelativeAction="/statistics/domain/view.action" modelAttribute="domainStatisticForm" data-form="resource">
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="Targetgroups"/></h2>
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
                    <label class="control-label" for="targetId"><mvc:message code="Target"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="targetId" id="targetId" size="1" cssClass="form-control js-select">
                        <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                        <mvc:options items="${targetList}" itemValue="id" itemLabel="targetName"/>
                    </mvc:select>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="mailingListId"><mvc:message code="Mailinglist"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="mailingListId" id="mailingListId" size="1" cssClass="form-control js-select">
                        <mvc:option value="0"><mvc:message code="statistic.All_Mailinglists"/></mvc:option>
                        <mvc:options items="${mailingLists}" itemValue="id" itemLabel="shortname"/>
                    </mvc:select>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="maxDomainNum"><mvc:message code="domains.max"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="maxDomainNum" id="maxDomainNum" size="1" cssClass="form-control js-select">
                        <mvc:option value="5" label="5"/>
                        <mvc:option value="10" label="10"/>
                        <mvc:option value="15" label="15"/>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <div class="headline">
                <mvc:select path="topLevelDomain" data-form-submit="">
                    <mvc:option value="false"><mvc:message code="Domains"/></mvc:option>
                    <mvc:option value="true"><mvc:message code="ToplevelDomains"/></mvc:option>
                </mvc:select>
            </div>
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
                            <a href="${birtStatisticUrlWithoutFormat}&__format=csv" tabindex="-1" data-prevent-load="">
                                <i class="icon icon-file-excel-o"></i> <mvc:message code='export.message.csv'/>
                            </a>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content">
            <iframe src="${birtStatisticUrlWithoutFormat}&__format=html" border="0" scrolling="auto" width="100%" height="100px" frameborder="0">
                Your Browser does not support IFRAMEs, please update!
            </iframe>
        </div>
    </div>

</mvc:form>
