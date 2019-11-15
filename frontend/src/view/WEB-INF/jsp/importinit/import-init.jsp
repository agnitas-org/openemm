<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>

<%--@elvariable id="emmLayoutBase" type="org.agnitas.beans.EmmLayoutBase"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>

<div class="col-md-offset-3 col-md-6">
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="mailing.mode.select"/>:</h2>
        </div>

        <div class="tile-content">
            <ul class="link-list">
                <li>
                    <html:link page="/newimportwizard.do?action=1" styleClass="link-list-item">
                        <div class="thumbnail">
                            <img alt="" class="media-object" src="${emmLayoutBase.imagesURL}/facelift/agn_import_standard.png">
                        </div>
                        <div class="media-body">
                            <p class="headline">
                                <bean:message key="import.standard"/>
                            </p>
                            <p class="description">
                                <bean:message key="import.standard.description"/>
                            </p>
                        </div>
                        <i class="nav-arrow icon icon-angle-right"></i>
                    </html:link>
                </li>
                <li>
                    <html:link page="/importwizard.do?action=1" styleClass="link-list-item">
                        <div class="thumbnail">
                            <img alt="" class="media-object" src="${emmLayoutBase.imagesURL}/facelift/agn_mailing-new-assistant.png">
                        </div>
                        <div class="media-body">
                            <p class="headline">
                                <bean:message key="import.Wizard"/>
                            </p>
                            <p class="description">
                                <bean:message key="import.wizard.description"/>
                            </p>
                        </div>
                        <i class="nav-arrow icon icon-angle-right"></i>
                    </html:link>
                </li>
            </ul>
        </div>
    </div>
</div>
