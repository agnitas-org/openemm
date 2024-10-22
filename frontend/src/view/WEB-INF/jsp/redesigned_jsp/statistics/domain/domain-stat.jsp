<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="domainStatisticForm" type="com.agnitas.emm.core.birtstatistics.domain.form.DomainStatisticForm"--%>
<%--@elvariable id="targetList" type="java.util.List"--%>
<%--@elvariable id="mailingLists" type="java.util.List"--%>
<%--@elvariable id="birtStatisticUrlWithoutFormat" type="java.lang.String"--%>

<mvc:form cssClass="tiles-container flex-column" id="stat-form" servletRelativeAction="/statistics/domain/view.action"
          data-form="resource" modelAttribute="domainStatisticForm" data-editable-view="${agnEditViewKey}">

    <div id="filter-tile" class="tile h-auto flex-none" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="report.mailing.filter" /></h1>
        </div>

        <div class="tile-body">
            <div class="row g-3">
                <div class="col">
                    <label class="form-label" for="targetId"><mvc:message code="Target"/></label>

                    <mvc:select path="targetId" id="targetId" size="1" cssClass="form-control js-select">
                        <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                        <mvc:options items="${targetList}" itemValue="id" itemLabel="targetName"/>
                    </mvc:select>
                </div>

                <div class="col">
                    <label class="form-label" for="mailingListId"><mvc:message code="Mailinglist"/></label>
                    <mvc:select path="mailingListId" id="mailingListId" size="1" cssClass="form-control js-select">
                        <mvc:option value="0"><mvc:message code="statistic.All_Mailinglists"/></mvc:option>
                        <mvc:options items="${mailingLists}" itemValue="id" itemLabel="shortname"/>
                    </mvc:select>
                </div>

                <div class="col">
                    <label class="form-label" for="maxDomainNum"><mvc:message code="domains.max"/></label>
                    <mvc:select path="maxDomainNum" id="maxDomainNum" size="1" cssClass="form-control js-select">
                        <mvc:option value="5" label="5"/>
                        <mvc:option value="10" label="10"/>
                        <mvc:option value="15" label="15"/>
                        <mvc:option value="20" label="20"/>
                        <mvc:option value="25" label="25"/>
                        <mvc:option value="50" label="50"/>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>

    <div id="overview-tile" class="tile" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="report.mailing.statistics.select"/></h1>
            <div class="tile-title-controls">
                <mvc:select path="topLevelDomain" cssClass="form-control js-select" data-form-submit="" data-select-options="dropdownAutoWidth: true, width: 'auto'">
                    <mvc:option value="false"><mvc:message code="Domains"/></mvc:option>
                    <mvc:option value="true"><mvc:message code="ToplevelDomains"/></mvc:option>
                </mvc:select>
            </div>
        </div>
        <div class="tile-body p-2 js-scrollable" style="overflow-y: auto !important;">
            <iframe src="${birtStatisticUrlWithoutFormat}&__format=html" border="0" scrolling="auto" style="width: 100%; height: 100px" frameborder="0">
                Your Browser does not support IFRAMEs, please update!
            </iframe>
        </div>
    </div>
</mvc:form>
