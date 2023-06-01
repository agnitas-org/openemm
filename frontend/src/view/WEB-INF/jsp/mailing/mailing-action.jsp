<%--checked --%>
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>

<agn:agnForm action="/mailingbase" styleClass="top_10">
    <html:hidden property="mailingID"/>
    <html:hidden property="action"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="action.Action"/>
            </h2>
        </div>

        <div class="tile-content" data-form-content>
            <div class="table-wrapper">
                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        id="action"
                        name="${mailingBaseForm.actions}">

                    <display:column titleKey="action.Action" headerClass="js-table-sort">
                        <span class="ie7hack">${action["action_name"]}</span>
                    </display:column>

                    <display:column titleKey="mailing.URL" headerClass="js-table-sort">
                        <span class="ie7hack">
                                ${action["url"]}
                        </span>

                        <c:url var="actionUrl" value="/action/${action['action_id']}/view.action"/>

                        <a href="${actionUrl}" class="hidden js-row-show">
                            ${action["url"]}
                        </a>
                    </display:column>

                </display:table>
            </div>
        </div>
    </div>

</agn:agnForm>
