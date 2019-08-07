<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib prefix="agn"     uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="logic"   uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>

<%--@elvariable id="newImportWizardForm" type="com.agnitas.web.forms.ComNewImportWizardForm"--%>
<%--@elvariable id="importProfileMode" type="java.lang.Integer"--%>

<agn:agnForm action="/newimportwizard" enctype="multipart/form-data" styleId="newimportwizard" data-form="resource" id="newImportWizardForm">
    <input type="hidden" id="preview_proceed" name="preview_proceed" value=""/>
    <input type="hidden" id="preview_back" name="preview_back" value=""/>
    <input type="hidden" id="defaultProfileId" name="defaultProfileId" value="${newImportWizardForm.defaultProfileId}"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="default.Preview"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="well well-info block"><bean:message key="import.title.preview"/></div>
            <div class="vspace-top-10"></div>
            <div class="table-wrapper">
                <table class="table table-bordered table-striped">
                    <thead>
                        <tr>
                            <logic:iterate id="row" name="newImportWizardForm" property="previewParsedContent" scope="session" indexId="counter">
                                <logic:iterate id="element" name="row" scope="page">
                                    <c:if test="${counter==0}">
                                        <th><bean:write name="element"/></th>
                                    </c:if>
                                </logic:iterate>
                            </logic:iterate>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="row" items="${newImportWizardForm.previewParsedContent}" begin="1" end="5">
                            <tr>
                                <c:forEach var="element" items="${row}">
                                    <td>${element}</td>
                                </c:forEach>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <c:if test="${importProfileMode ne 5}">
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="settings.mailinglist"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="import.SubscribeLists"/></label>
                </div>
                <div class="col-sm-8">
                    <ul class="list-group">
                        <c:set var="enforceMailinglist" value="${newImportWizardForm.enforceMailinglist}"/>
                        <c:choose>
                            <c:when test="${empty enforceMailinglist}">
                                <c:forEach var="mlist" items="${newImportWizardForm.allMailingLists}">
                                    <li class="list-group-item">
                                        <label class="checkbox-inline">
                                            <html:checkbox property="selectedMailinglist[${mlist.id}]"/>${mlist.shortname}
                                        </label>
                                    </li>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <li class="list-group-item">
                                    <label class="checkbox-inline">
                                        <html:hidden property="selectedMailinglist[${enforceMailinglist.id}]" value="1"/>
                                        <input type="checkbox" checked disabled name="mailinglist[${enforceMailinglist.id}]"/>
                                            ${enforceMailinglist.shortname}
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
</agn:agnForm>
