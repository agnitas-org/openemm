<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComRecipientAction" %>
<%@ page import="com.agnitas.web.ComRecipientForm" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common" prefix="emm"%>

<%--@elvariable id="recipientForm" type="com.agnitas.web.ComRecipientForm"--%>

<c:set var="ACTION_MAILINGS_VIEW" value="<%= ComRecipientAction.ACTION_MAILINGS_VIEW %>"/>
<c:set var="ACTION_MODAL_MAILING_DELIVERY_HISTORY" value="<%= ComRecipientAction.ACTION_MODAL_MAILING_DELIVERY_HISTORY %>"/>
<c:set var="mailingTypeNames" value="<%= ComRecipientForm.MAILING_TYPE_NAMES %>"/>

<html:form action="/recipient">
    <html:hidden property="action"/>
    <html:hidden property="numberOfRowsChanged"/>
    <html:hidden property="recipientID"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "recipient-mailing-history-overview": {
                "rows-count": ${recipientForm.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="default.search"/>
            </h2>

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

            <c:set var="dateColumn" value="{0,date,${localeTablePattern}}"/>
            <c:set var="deliveryDateAvailable" value="false"/>
            <c:forEach var="recipientMailing" items="${recipientMailings.list}">
                <c:if test="${not empty recipientMailing.deliveryDate}">
                    <c:set var="deliveryDateAvailable" value="true"/>
                </c:if>
            </c:forEach>

            <div class="table-wrapper">
                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        id="recipientMailing"
                        name="recipientMailings"
                        sort="external"
                        partialList="true"
                        pagesize="${recipientForm.numberOfRows}"
                        requestURI="/recipient.do?action=${ACTION_MAILINGS_VIEW}&recipientID=${recipientForm.recipientID}&__fromdisplaytag=true"
                        excludedParams="*"
                        size="${recipientMailings.fullListSize}">

                    <display:column headerClass="js-table-sort" titleKey="mailing.senddate" sortable="true" format="${dateColumn}" property="sendDate" />

                    <display:column headerClass="js-table-sort" titleKey="default.Type" sortable="true" sortProperty="mailingType">
                        <c:set var="mailingTypeName" value="${mailingTypeNames[recipientMailing.mailingType]}"/>
                        <c:if test="${not empty mailingTypeName}">
                            <bean:message key="${mailingTypeName}"/>
                        </c:if>
                    </display:column>

                    <display:column headerClass="js-table-sort" titleKey="default.Name" sortable="true">
                        <c:choose>
                            <c:when test="${ableToViewDeliveryHistory}">
                                <c:url var="deleteProfileLink" value="/recipient.do">
                                    <c:param name="action" value="${ACTION_MODAL_MAILING_DELIVERY_HISTORY}"/>
                                    <c:param name="recipientID" value="${recipientForm.recipientID}"/>
                                    <c:param name="mailingId" value="${recipientMailing.mailingId}"/>
                                    <c:param name="mailingName" value="${recipientMailing.shortName}"/>
                                </c:url>

                                <a href="${deleteProfileLink}" class="btn btn-regular js-confirm">
                                    <i class="icon icon-share-square-o"></i>
                                    <span>${recipientMailing.shortName}</span>
                                </a>
                            </c:when>
                            <c:otherwise>
                                ${recipientMailing.shortName}
                            </c:otherwise>
                        </c:choose>
                    </display:column>

                    <display:column headerClass="js-table-sort" titleKey="mailing.Subject" sortable="true" property="subject" />

                    <c:if test="${deliveryDateAvailable}">
                        <display:column headerClass="js-table-sort" titleKey="recipient.Mailings.deliverydate" sortable="true" format="${dateColumn}" property="deliveryDate" />
                    </c:if>

                    <display:column headerClass="js-table-sort" titleKey="recipient.Mailings.openings" sortable="true" property="numberOfOpenings" />

                    <display:column headerClass="js-table-sort" titleKey="recipient.Mailings.clicks" sortable="true" property="numberOfClicks" />

                </display:table>
            </div>

        </div>
    </div>
</html:form>
