<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="org.agnitas.util.importvalues.ImportMode" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="importProfileForm" type="org.agnitas.web.forms.ImportProfileForm"--%>

<c:set var="isMailingListSelectionEditable" value="${importProfileForm.profile.actionForNewRecipients eq 0}"/>

<div id="recipient-autoimport-mailinglists-tile" class="tile" data-hide-by-select="#import_mode_select" data-hide-by-select-values="<%= ImportMode.TO_BLACKLIST.getIntValue() %>">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tile-recipient-autoimport-mailinglists">
            <i class="tile-toggle icon icon-angle-up"></i>
            <bean:message key="recipient.Mailinglists"/>
        </a>
    </div>

    <div class="tile-content tile-content-forms" id="tile-recipient-autoimport-mailinglists">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label">
                    <bean:message key="recipient.Mailinglists"/>
                </label>
            </div>

            <div class="col-sm-8 ${isMailingListSelectionEditable ? '' : 'hidden'}" id="mailinglists">
                <ul class="list-group">
                    <c:forEach var="mailinglist" items="${importProfileForm.availableMailinglists}">
                        <li class="list-group-item">
                            <label class="checkbox-inline">
                                <html:checkbox property="mailinglist[${mailinglist.id}]"/>
                                ${fn:escapeXml(mailinglist.shortname)}
                            </label>
                        </li>
                    </c:forEach>
                </ul>
            </div>

            <div class="col-sm-8 ${isMailingListSelectionEditable ? 'hidden' : ''}" id="mailinglists-to-show">
                <ul class="list-group">
                    <c:forEach var="mailinglist" items="${importProfileForm.availableMailinglists}">
                        <li class="list-group-item">
                            <label class="checkbox-inline">
                                <c:choose>
                                    <c:when test="${importProfileForm.mailinglistsToShow.contains(mailinglist.id)}">
                                        <input type="checkbox" checked="checked" disabled="disabled"/>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="checkbox" disabled="disabled"/>
                                    </c:otherwise>
                                </c:choose>
                                ${fn:escapeXml(mailinglist.shortname)}
                            </label>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </div>
</div>
