<%@ page import="org.agnitas.web.SalutationAction"  errorPage="/error.do" %>
<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_VIEW" value="<%= SalutationAction.ACTION_VIEW%>" scope="page"/>
<c:set var="ACTION_LIST" value="<%= SalutationAction.ACTION_LIST%>" scope="page"/>

<agn:agnForm action="/salutation" id="salutationForm" data-form="resource">
    <html:hidden property="salutationID"/>
    <html:hidden property="action"/>
    <input type="hidden" id="save" name="save" value=""/>
    <c:choose>
    	<c:when test="${salutationForm.salutationCompanyID ne 0}">
	        <c:set var="dontEdit" value="false"/>
    	</c:when>
    	<c:otherwise>
        	<c:set var="dontEdit" value="true"/>
    	</c:otherwise>
	</c:choose>
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <c:choose>
                    <c:when test="${salutationForm.salutationID eq 0}">
                        <bean:message key="default.salutation.shortname"/>
                    </c:when>
                    <c:otherwise>
                        <bean:message key="settings.EditFormOfAddress"/>
                    </c:otherwise>
                </c:choose>
            </h2>
        </div>
        <div class="tile-content tile-content-forms">
            <c:if test="${salutationForm.salutationID ne 0}">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="" class="control-label">
                            ID:
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <p class="form-control-static">${salutationForm.salutationID}</p>
                    </div>
                </div>
            </c:if>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-salutation-description" class="control-label">
                        <bean:message key="Description"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:text readonly="${dontEdit}" styleId="recipient-salutation-description" styleClass="form-control" property="shortname" />
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-salutation-male" class="control-label">
                        GENDER=0 (<bean:message key="Male"/>)
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:text readonly="${dontEdit}" styleId="recipient-salutation-male" styleClass="form-control" property="salMale" />
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-salutation-female" class="control-label">
                        GENDER=1 (<bean:message key="Female"/>)
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:text readonly="${dontEdit}" styleId="recipient-salutation-female" styleClass="form-control" property="salFemale" />
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-salutation-unknown" class="control-label">
                        GENDER=2 (<bean:message key="Unknown"/>)
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:text readonly="${dontEdit}" styleId="recipient-salutation-unknown" styleClass="form-control" property="salUnknown" />
                </div>
            </div>

            <emm:ShowByPermission token="recipient.gender.extended">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="recipient-salutation-practice" class="control-label">
                            GENDER=4 (<bean:message key="PracticeShort"/>)
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:text readonly="${dontEdit}" styleId="recipient-salutation-practice" styleClass="form-control" property="salPractice" />
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="recipient-salutation-company" class="control-label">
                            GENDER=5 (<bean:message key="recipient.gender.5.short"/>)
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <html:text readonly="${dontEdit}" styleId="recipient-salutation-company" styleClass="form-control" property="salCompany" />
                    </div>
                </div>
            </emm:ShowByPermission>
        </div>
    </div>
</agn:agnForm>
