<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<agn:agnForm action="/mailing_compare" data-form-type="search">
    <html:hidden property="action"/>
    <html:hidden styleId="reportFormat" property="reportFormat" value="html"/>
    <c:forEach var="mailing" items="${compareMailingForm.mailings}">
        <input type="hidden" name="MailCompID_${mailing}"/>
    </c:forEach>

    <div class="tile">
        <div class="tile-header">
            <a class="headline" href="#" data-toggle-tile="#tile-targetGroup">
                <i class="icon tile-toggle icon-angle-up"></i>
                <bean:message key="Targets"/>
            </a>
            <ul class="tile-header-actions">
                <li>
                    <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                        <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Refresh"/></span>
                    </button>
                </li>
            </ul>
        </div>
        <div id="tile-targetGroup" class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <bean:message key="Targetgroups"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <c:set var="addTargetGroupMessage" scope="page">
                        <bean:message key="addTargetGroup" />
                    </c:set>
                    <agn:agnSelect property="selectedTargets"  styleClass="form-control js-select" multiple="" data-placeholder="${addTargetGroupMessage}">
                        <c:forEach var="target" items="${targetGroups}" varStatus="rowCounter">
                            <html:option value="${target.id}">${target.targetName}</html:option>
                        </c:forEach>
                    </agn:agnSelect >
                </div>
            </div>
        </div>
    </div>
    <div class="tile">
        <jsp:include page="birt-mailing-comp-stat-recipientfilter.jsp" flush="false">
            <jsp:param name="isCompareBtnShow" value="false"/>
        </jsp:include>
        <div class="tile-content">
            <div class="content_element_container">
                <iframe src="${compareMailingForm.reportUrl}" border="0" scrolling="auto" frameborder="0" style="width: 100%">
                    Your Browser does not support IFRAMEs, please update!
                </iframe>
            </div>
        </div>
    </div>
</agn:agnForm>
