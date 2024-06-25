<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.archive.forms.MailingArchiveSimpleActionForm"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                <h4 class="modal-title">
                    <c:choose>
                        <c:when test="${form.isCampaign}">
                            <mvc:message code="mailing.archive"/>: ${form.shortname}
                        </c:when>
                        <c:otherwise>
                            <c:choose>
                                <c:when test="${form.isTemplate}">
                                    <mvc:message code="Template"/>: ${form.shortname}
                                </c:when>
                                <c:otherwise>
                                    <mvc:message code="Mailing"/>: ${form.shortname}
                                </c:otherwise>
                            </c:choose>
                        </c:otherwise>
                    </c:choose>
                </h4>
            </div>

            <mvc:form servletRelativeAction="/mailing/archive/delete.action" modelAttribute="form" method="DELETE">
                <mvc:hidden path="mailingId"/>
                <mvc:hidden path="campaignId"/>
                <mvc:hidden path="isCampaign"/>

                <div class="modal-body">
                    <c:choose>
                        <c:when test="${form.isCampaign}">
                            <mvc:message code="campaign.DeleteCampaignQuestion"/>
                        </c:when>

                        <c:otherwise>
                            <c:choose>
                                <c:when test="${form.isTemplate}">
                                    <mvc:message code="mailing.Delete_Template_Question"/>
                                </c:when>
                                <c:otherwise>
                                    <mvc:message code="mailing.MailingDeleteQuestion"/>
                                </c:otherwise>
                            </c:choose>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="button.Delete"/></span>
                        </button>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
