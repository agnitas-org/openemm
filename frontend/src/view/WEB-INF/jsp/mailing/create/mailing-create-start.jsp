<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="emmLayoutBase" type="com.agnitas.beans.EmmLayoutBase"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>

<emm:setAbsolutePath var="absoluteImagePath" path="${emmLayoutBase.imagesURL}"/>

<div class="col-md-offset-3 col-md-6">
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="mailing.mode.select"/>:</h2>
        </div>

        <div class="tile-content">
            <ul class="link-list">
            <%@include file="mailing-create-grid.jspf" %>
            
            <emm:ShowByPermission token="mailing.classic">
                <li>
                    <c:url var="standardBtnRef" value="/mailing/templates.action?keepForward=${workflowId > 0}" />
                    <a href="${standardBtnRef}" class="link-list-item">
                        <div class="thumbnail">
                            <img alt="" class="media-object" src="${absoluteImagePath}/facelift/agn_mailing-new-standard.png">
                        </div>
                        <div class="media-body">
                            <p class="headline">
                                <mvc:message code="mailing.wizard.Normal"/>
                            </p>
                            <p class="description">
                                <mvc:message code="mailing.wizard.NormalDescription"/>
                            </p>
                        </div>
                        <i class="nav-arrow icon icon-angle-right"></i>
                    </a>
                </li>
                </emm:ShowByPermission>
                <emm:ShowByPermission token="mailing.import">
                    <li>
                        <c:url var="importMailingLink" value="/import/mailing.action" />
                        <a href="${importMailingLink}" class="link-list-item">
                            <div class="thumbnail">
                                <img alt="" class="media-object" src="${absoluteImagePath}/file-download.png">
                            </div>
                            <div class="media-body">
                                <p class="headline">
                                    <mvc:message code="import.csv_upload"/>
                                </p>
                                <p class="description">
                                    <mvc:message code="mailing.import"/>
                                </p>
                            </div>
                            <i class="nav-arrow icon icon-angle-right"></i>
                        </a>
                    </li>
                </emm:ShowByPermission>
            </ul>
        </div>
    </div>
</div>
