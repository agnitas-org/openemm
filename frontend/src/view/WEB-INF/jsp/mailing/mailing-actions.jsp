<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="agn"     uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="actions" type="java.util.List<java.util.Map>"--%>
<%--@elvariable id="action" type="java.util.Map"--%>

<div class="tile top_10">
    <div class="tile-header">
        <h2 class="headline">
            <mvc:message code="action.Action"/>
        </h2>
    </div>
    <div class="tile-content">
        <div class="table-wrapper">
            <display:table
                    class="table table-bordered table-striped table-hover js-table"
                    id="action"
                    name="${actions}">

                <display:column titleKey="action.Action" headerClass="js-table-sort">
                    <span class="ie7hack">${action["action_name"]}</span>
                </display:column>

                <display:column titleKey="mailing.URL" headerClass="js-table-sort">
                    <span class="ie7hack">${action["url"]}</span>
                    <a href="<c:url value="/action/${action['action_id']}/view.action"/>" class="hidden js-row-show">
                        ${action["url"]}
                    </a>
                </display:column>

            </display:table>
        </div>
    </div>
</div>
