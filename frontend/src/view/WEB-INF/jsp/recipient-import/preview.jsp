<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.imports.form.RecipientImportForm"--%>
<%--@elvariable id="parsedContent" type="java.util.List<java.ulit.List<java.lang.String>>"--%>
<%--@elvariable id="enforcedMailinglist" type="com.agnitas.beans.Mailinglist"--%>
<%--@elvariable id="mailinglists" type="java.util.List<com.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="possibleToSelectMailinglist" type="java.lang.Boolean"--%>

<mvc:form id="import-form" servletRelativeAction="/recipient/import/execute.action" enctype="multipart/form-data" data-form="resource" modelAttribute="form">
    <mvc:hidden path="profileId" />
    <mvc:hidden path="attachmentCsvFileID" />

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="default.Preview"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="well well-info block"><mvc:message code="import.title.preview"/></div>
            <div class="vspace-top-10"></div>
            <div class="table-wrapper">
                <table class="table table-bordered table-striped">
                    <thead>
                    <tr>
                        <c:forEach items="${parsedContent}" var="row" varStatus="status">
                            <c:forEach items="${row}" var="element">
                                <c:if test="${status.index eq 0}">
                                    <th>${element}</th>
                                </c:if>
                            </c:forEach>
                        </c:forEach>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="row" items="${parsedContent}" begin="1" end="5">
                        <tr>
                            <c:forEach var="element" items="${row}">
                                <td>${fn:escapeXml(element)}</td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <c:if test="${possibleToSelectMailinglist}">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><mvc:message code="settings.mailinglist"/></h2>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><mvc:message code="import.SubscribeLists"/></label>
                    </div>
                    <div class="col-sm-8">
                        <ul class="list-group">
                            <c:choose>
                                <c:when test="${empty enforcedMailinglist}">
                                    <c:forEach var="mlist" items="${mailinglists}">
                                        <li class="list-group-item">
                                            <label class="checkbox-inline">
                                                <mvc:checkbox path="selectedMailinglist[${mlist.id}]" value="true" />${mlist.shortname}
                                            </label>
                                        </li>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <li class="list-group-item">
                                        <label class="checkbox-inline">
                                            <mvc:hidden path="selectedMailinglist[${enforcedMailinglist.id}]" value="true"/>
                                            <input type="checkbox" checked disabled name="mailinglist[${enforcedMailinglist.id}]"/>
                                                ${enforcedMailinglist.shortname}
                                        </label>
                                    </li>
                                </c:otherwise>
                            </c:choose>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </c:if>
</mvc:form>
