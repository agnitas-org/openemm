<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingID" type="java.lang.Integer"--%>
<%--@elvariable id="clearanceThreshold" type="java.lang.Integer"--%>
<%--@elvariable id="statusOnErrorEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="clearanceEmail" type="java.lang.String"--%>
<%--@elvariable id="autoImports" type="java.util.List<org.agnitas.emm.core.autoimport.bean.AutoImportLight>"--%>
<%--@elvariable id="autoImportId" type="java.lang.Integer"--%>

<c:set var="clearanceThreshold" value="${clearanceThreshold gt 0 ? clearanceThreshold : ''}"/>
<c:set var="isNotificationEnabled" value="${not empty clearanceEmail}"/>

<div class="modal modal-wide" id="security-settings-modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal" id="close-security-settings">
                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
                <h4 class="modal-title"><mvc:message code="mailing.send.security.notification" /></h4>
            </div>
            <mvc:form servletRelativeAction="/mailing/ajax/${mailingID}/saveSecuritySettings.action" data-form="resource" id="security-settings-form">
                <div class="tile" data-field="toggle-vis">
                    <div class="tile-content tile-content-forms">
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label" for="enable-notification">
                                    <mvc:message code="mailing.notification.enable"/>
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <label class="toggle">

                                    <input type="checkbox" name="enableNotifications" id="enable-notification" ${isNotificationEnabled ? "checked=checked" : ""} data-field-vis=""
                                         data-field-vis-show="#notification-related-data">
                                    <div class="toggle-control"></div>
                                    <div class="hidden" data-field-vis-default="" data-field-vis-hide="#notification-related-data"></div>
                                </label>
                            </div>
                        </div>

                        <div id="notification-related-data">
                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label class="control-label" for="enable-status-on-error">
                                        <mvc:message code="mailing.SendStatusOnErrorOnly" />
                                        <button type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/mailing/SendStatusOnErrorOnly.xml"></button>
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <label class="toggle">
                                        <input type="checkbox" name="enableNoSendCheckNotifications"
                                               id="enable-status-on-error" ${statusOnErrorEnabled ? "checked=checked" : ""}>
                                        <div class="toggle-control"></div>
                                    </label>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label class="control-label" for="clearanceThreshold">
                                        <mvc:message code="mailing.autooptimization.threshold"/>
                                        <button type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/mailing/Threshold.xml"></button>
                                    </label>
                                </div>
                                <div class="col-sm-6">
                                    <mvc:message var="placeholder" code="mailing.send.threshold"/>
                                    <input type="text" class="form-control" id="clearanceThreshold" name="clearanceThreshold"
                                           value="${clearanceThreshold}" placeholder="${placeholder}">
                                </div>
                            </div>

                            <input type="hidden" id="required-import-id" name="autoImportId" value="${autoImportId}"/>

                            <emm:ShowByPermission token="recipient.import.auto.mailing">
                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label class="control-label" for="required-auto-import"><mvc:message code="autoImport.autoImport" /></label>
                                    </div>
                                    <div class="col-sm-6">
                                        <select id="required-auto-import" class="form-control js-select" data-sync-from="#required-auto-import" data-sync-to="#required-import-id">
                                            <option value="0">---</option>
                                            <c:forEach var="autoImport" items="${autoImports}">
                                                <option value="${autoImport.autoImportId}" ${autoImport.autoImportId eq autoImportId ? "selected" : ""} >
                                                        ${autoImport.shortname}
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                            </emm:ShowByPermission>

                            <div class="tile-separator"></div>

                            <input type="hidden" name="clearanceEmail" value="${clearanceEmail}"/>

                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label class="control-label" for="clearanceEmail">
                                        <mvc:message code="Recipients" />
                                        <button type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/mailing/SendStatusEmail.xml"></button>
                                    </label>
                                </div>
                                <div class="col-sm-6">
                                    <div id="clearanceEmail"
                                         data-controller="email-list-controller"
                                         data-initializer="email-list-initializer"
                                         data-target-field="clearanceEmail">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="tile-footer" data-sizing="bottom">
                        <button type="button" class="btn btn-large pull-left" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>

                        <button type="button" class="btn btn-large btn-primary pull-right" data-action="save-security-settings">
                            <i class="icon icon-save"></i>
                            <span class="text"><mvc:message code="button.Save"/></span>
                        </button>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
