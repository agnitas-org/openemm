<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.util.ImportUtils" %>
<%@ page import="java.util.Map" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>

<%--@elvariable id="newImportWizardForm" type="com.agnitas.web.forms.ComNewImportWizardForm"--%>

<agn:agnForm action="/newimportwizard" styleId="newimportwizard" data-form="resource">
    <html:hidden property="numberOfRowsChanged"/>
    <input type="hidden" id="edit_page_save" name="edit_page_save" value=""/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "import-wizard-errors-overview": {
                "rows-count": ${newImportWizardForm.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="import.edit.data"/></h2>
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
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show" /></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content">
            <div class="table-control">
                <div class="well well-info block"><bean:message key="import.title.error_edit" /><br><html:errors/></div>
            </div>
            <div class="table-wrapper">
                <display:table class="table table-bordered table-striped table-form js-table"
                               pagesize="${newImportWizardForm.numberOfRows}"
                               id="recipient"
                               name="recipientList"
                               sort="external"
                               excludedParams="*"
                               requestURI="/newimportwizard.do?action=4&__fromdisplaytag=true"
                               partialList="true"
                               size="${recipientList.fullListSize}">
                               
					<display:column class="import_errors_head_name" headerClass="import_errors_head_name" titleKey="errorReason">
						<bean:message key="${recipient.ERROR_EDIT_REASON_KEY_RESERVED}" />
					</display:column>
					
					<c:forEach items="${newImportWizardForm.columns}" var="item">
                        <c:if test="${item.importedColumn}">
                            <display:column class="import_errors_head_name" headerClass="import_errors_head_name" title="${item.colName}">
                                <div class="has-warning has-feedback">
                                    <c:set scope="page" value="${item.colName}" var="propertyName"/>
                                    <%
                                    if (ImportUtils.isFieldValid((String) pageContext.getAttribute("propertyName"), (Map<String, Object>) recipient)) {
                                    %>
                                    <c:out value='<%= ((Map)recipient).get((String) pageContext.getAttribute("propertyName")) %>'/>
                                    <%
                                    } else {
                                    %>
                                    <c:set var="id" value="${recipient.ERROR_EDIT_RECIPIENT_EDIT_RESERVED.temporaryId}_${item.colName}"/>
                                    <input type="text" id="${id}"
                                           name="changed_recipient_${recipient.ERROR_EDIT_RECIPIENT_EDIT_RESERVED.temporaryId}/RESERVED/${item.colName}"
                                           value='<%= ((String)((Map)recipient).get((String) pageContext.getAttribute("propertyName"))) %>'
                                           class="form-control">
                                    <span class="icon icon-state-warning form-control-feedback"></span>
                                    <%
                                        }
                                    %>
                                </div>
                            </display:column>
                        </c:if>
                    </c:forEach>
                </display:table>
            </div>
        </div>
    </div>
</agn:agnForm>
