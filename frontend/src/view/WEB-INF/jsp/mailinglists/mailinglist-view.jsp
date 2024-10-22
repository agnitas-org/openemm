<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailinglistForm" type="com.agnitas.emm.core.mailinglist.form.MailinglistForm"--%>
<%--@elvariable id="birtStatisticUrlWithoutFormat" type="java.lang.String"--%>
<%--@elvariable id="monthList" type="java.util.List"--%>
<%--@elvariable id="yearlist" type="java.util.List"--%>

<c:set var="isNew" value="${mailinglistForm.id eq 0}"/>

<mvc:form servletRelativeAction="/mailinglist/save.action"
          modelAttribute="mailinglistForm"
          data-form="resource"
          data-form-focus="shortname">

    <mvc:hidden path="id"/>
    <mvc:hidden path="targetId"/>
    <mvc:hidden path="mediatypes"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="settings.EditMailinglist"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="shortname" class="control-label">
                        <mvc:message var="nameMsg" code="default.Name"/>
                        ${nameMsg}*
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="shortname" cssClass="form-control" id="shortname" placeholder="${nameMsg}"/>
                </div>
            </div>

            <%@include file="./mailing-frequency-toggle.jspf"%>

            <div class="form-group">
                <div class="col-sm-4">
                    <label for="description" class="control-label">
                        <mvc:message var="descriptionMsg" code="default.description"/>
                        ${descriptionMsg}
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:textarea path="description" id="description" cssClass="form-control" rows="5" placeholder="${descriptionMsg}"/>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label for="description" class="control-label">
                        <mvc:message var="senderEmailMsg" code="mailing.SenderEmail"/>
                        ${senderEmailMsg}
                    </label>
                </div>
                <div class="col-sm-8">
                    <%@include file="domain-addresses-dropdown.jspf" %>
                    <c:choose>
                        <c:when test="${domainAddressesDropdown eq null}">
                            <mvc:text path="senderEmail" cssClass="form-control" id="senderEmail" placeholder="${senderEmailMsg}"/>
                        </c:when>
                        <c:otherwise>
                            ${domainAddressesDropdown}
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label for="description" class="control-label">
                        <mvc:message var="replyToEmailMsg" code="mailing.ReplyEmail"/>
                        ${replyToEmailMsg}
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="replyEmail" cssClass="form-control" id="replyToEmail" placeholder="${replyToEmailMsg}"/>
                </div>
            </div>

            <%@include file="mailinglist-manage-approval-fragment.jspf.jsp" %>

        </div>
    </div>
    <c:if test="${not isNew or not empty mailinglistForm.statistic}">
        <mvc:hidden path="statistic.mailinglistId"/>
        <div class="tile">
            <div class="tile-header">
                <label class="headline">
                    <mvc:message code="Statistics"/>
                </label>
                <ul class="tile-header-actions">
                    <li class="dropdown">
                        <a class="dropdown-toggle" href="#" data-toggle="dropdown">
                            <i class="icon icon-cloud-download"></i>
                            <span class="text"><mvc:message code="Export"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>
                        <ul class="dropdown-menu">
                            <li class="dropdown-header"><mvc:message code="statistics.exportFormat"/></li>
                            <li>
                                <a href="${birtStatisticUrlWithoutFormat}&__format=csv" tabindex="-1" data-prevent-load="">
                                    <i class="icon icon-file-excel-o"></i>
                                    <mvc:message code='export.message.csv'/>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-controls">
                <div class="controls controls-left">
                    <div class="control">
                        <mvc:select path="statistic.startMonth" cssClass="form-control select2-offscreen">
                            <c:forEach items="${monthList}" var="month">
                                <mvc:option value="${month[0]}"><mvc:message code="${month[1]}"/></mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                    <div class="control">
                        <mvc:select path="statistic.startYear" cssClass="form-control select2-offscreen">
                            <c:forEach items="${yearlist}" var="year">
                                <mvc:option value="${year}"><c:out value="${year}"/></mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>

                <div class="controls controls-right">
                    <div class="control">
                        <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                            <i class="icon icon-refresh"></i>
                            <span class="text"><mvc:message code="button.Refresh"/></span>
                        </button>
                    </div>
                </div>

                <iframe id="birt-frame" src="${birtStatisticUrlWithoutFormat}&__format=html" border="0" scrolling="auto" width="100%" frameborder="0">
                    Your Browser does not support IFRAMEs, please update!
                </iframe>
            </div>
        </div>
    </c:if>
</mvc:form>
