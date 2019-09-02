<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="profileForm" type="com.agnitas.emm.core.profilefields.form.ProfileFieldForm"--%>
<%--@elvariable id="isNewField" type="java.lang.Boolean"--%>

<mvc:form servletRelativeAction="/profiledb/saveWizardField.action" id="wizard-step-7" data-controller="mailing-wizard-new" data-form="resource" modelAttribute="profileForm">
    <c:url var="backToTargetUrl" value="/profiledb/backToTarget.action"/>

    <div class="col-md-10 col-md-push-1 col-lg-8 col-lg-push-2">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline">
                    <i class="icon icon-file-o"></i>
                    <bean:message key="mailing.Wizard" />
                </h2>
                <ul class="tile-header-actions">
                    <li class="">
                        <ul class="pagination">
                            <li>
                                <a href="${backToTargetUrl}" >
                                    <i class="icon icon-angle-left"></i>
                                    <bean:message key="button.Back" />
                                </a>
                            </li>
                            <li class="disabled"><span>1</span></li>
                            <li class="disabled"><span>2</span></li>
                            <li class="disabled"><span>3</span></li>
                            <li class="disabled"><span>4</span></li>
                            <li class="disabled"><span>5</span></li>
                            <li class="disabled"><span>6</span></li>
                            <li class="active"><span>7</span></li>
                            <li class="disabled"><span>8</span></li>
                            <li class="disabled"><span>9</span></li>
                            <li class="disabled"><span>10</span></li>
                            <li class="disabled"><span>11</span></li>
                            <li>

                                <a href="${backToTargetUrl}" >
                                    <bean:message key="button.Proceed" />
                                    <i class="icon icon-angle-right"></i>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="form-group" data-field="required">
                    <div class="col-sm-4">
                        <label class="control-label" for="fieldShortname"><bean:message key="settings.FieldName"/></label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="shortname" cssClass="form-control" id="fieldShortname" maxlength="99" size="32" data-field-required=""/>
                    </div>
                </div>

                <div class="form-group has-info has-feedback">
                    <div class="col-sm-4">
                        <label class="control-label" for="fieldDescription"><bean:message key="Description"/></label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="description" id="fieldDescription" cssClass="form-control"/>
                        <span class="icon icon-state-info form-control-feedback"></span>
                        <div class="form-control-feedback-message"><bean:message key="profiledb.description.hint"/></div>
                    </div>
                </div>

                <div class="form-group" data-field="required">
                    <div class="col-sm-4">
                        <label class="control-label" for="fieldname"><bean:message key="settings.FieldNameDB"/></label>
                    </div>
                    <div class="col-sm-8">
                        <c:choose>
                            <c:when test="${isNewField}">
                                <mvc:text path="fieldname" cssClass="form-control" size="32" data-field-required=""/>
                            </c:when>
                            <c:otherwise>
                                <mvc:hidden path="fieldname"/>
                                <div class="form-control-static">${profileForm.fieldname}</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="fieldType"><bean:message key="default.Type"/></label>
                    </div>
                    <div class="col-sm-8">
                        <c:choose>
                            <c:when test="${isNewField}">
                                <mvc:select path="fieldType" size="1" id="fieldType" cssClass="form-control js-select" data-field-vis="">
                                    <mvc:option value="INTEGER" data-field-vis-hide="#fieldLengthDiv">
                                        <bean:message key="settings.fieldType.INTEGER"/>
                                    </mvc:option>
                                    <mvc:option value="VARCHAR" data-field-vis-show="#fieldLengthDiv">
                                        <bean:message key="settings.fieldType.VARCHAR"/>
                                    </mvc:option>
                                    <mvc:option value="DATE" data-field-vis-hide="#fieldLengthDiv">
                                        <bean:message key="settings.fieldType.DATE"/>
                                    </mvc:option>
                                </mvc:select>
                            </c:when>
                            <c:otherwise>
                                <mvc:hidden path="fieldType"/>
                                <div class="form-badge">
                                    <bean:message key="settings.fieldType.${profileForm.fieldType}"/>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="fieldDefault"><bean:message key="Default_Value"/></label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="fieldDefault" id="fieldDefault" cssClass="form-control" size="32"/>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="fieldNull"><bean:message key="settings.NullAllowed"/></label>
                    </div>
                    <div class="col-sm-8">
                        <c:choose>
                            <c:when test="${isNewField}">
                                <label class="toggle">
                                    <mvc:checkbox path="fieldNull" id="fieldNull"/>
                                    <div class="toggle-control"></div>
                                </label>
                            </c:when>
                            <c:otherwise>
                                <mvc:hidden path="fieldNull"/>
                                <div class="form-badge">
                                    <c:choose>
                                        <c:when test="${profileForm.fieldNull}">
                                            <bean:message key="Yes"/>
                                        </c:when>
                                        <c:otherwise>
                                            <bean:message key="No"/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="fieldSort"><bean:message key="FieldSort"/></label>
                    </div>
                    <div class="col-sm-8">
                        <select id="fieldSort" class="form-control js-select" name="fieldSort" >
                            <option value="1000"<c:if test="${profileForm.fieldSort == 1000}"> selected</c:if>>
                                <bean:message key="noSort"/></option>
                            <option value="1"<c:if test="${profileForm.fieldSort == 1}"> selected</c:if>><bean:message key="first"/></option>

                            <c:forEach var="field" items="${fieldsWithIndividualSortOrder}">
                                <option value='${field.sort + 1}' <c:if
                                        test="${profileForm.fieldSort == field.sort + 1}"> selected</c:if>>
                                    <bean:message key="after"/> ${field.shortname}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="form-group" data-field="validator">
                    <div class="col-sm-4">
                        <label class="control-label" for="fieldLength"><bean:message key="Length"/></label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="fieldLength" id="fieldLength" cssClass="form-control"
                                    data-field-validator="number"
                                    data-validator-options="min: 1, max: 4000, required: true, strict: true"/>
                    </div>
                </div>

                <emm:ShowByPermission token="profileField.visible">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="fieldVisible"><bean:message key="FieldVisible"/></label>
                        </div>
                        <div class="col-sm-8">
                            <label class="toggle">
                                <mvc:checkbox path="fieldVisible" id="fieldVisible"/>
                                <div class="toggle-control"></div>
                            </label>
                        </div>
                    </div>
                </emm:ShowByPermission>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="line"><bean:message key="line_after"/></label>
                    </div>
                    <div class="col-sm-8">
                        <label class="toggle">
                            <mvc:checkbox path="line" id="line"/>
                            <div class="toggle-control"></div>
                        </label>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="interest"><bean:message key="FieldIsInterest"/></label>
                    </div>
                    <div class="col-sm-8">
                        <label class="toggle">
                            <mvc:checkbox path="interest" id="interest"/>
                            <div class="toggle-control"></div>
                        </label>
                    </div>
                </div>
            </div>
            <div class="tile-footer">
                <input type="hidden" name="save" value=""/>
                <button class="btn btn-large pull-left">
                    <bean:message key="button.Save"/>
                </button>



                <a href="${backToTargetUrl}" class="btn btn-large btn-primary pull-right" >
                    <bean:message key="button.Proceed"/>
                    <i class="icon icon-angle-right"></i>
                </a>
                <span class="clearfix"></span>
            </div>
        </div>
    </div>
</mvc:form>
