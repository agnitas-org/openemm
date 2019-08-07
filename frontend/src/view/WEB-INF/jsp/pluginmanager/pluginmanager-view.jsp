<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="mvc"     uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="pluginForm" type="com.agnitas.emm.core.pluginmanager.form.PluginForm"--%>

<div class="tile">
    <div class="tile-header">
        <h2 class="headline"><bean:message key="pluginmanager.information" />
    </div>

    <div class="tile-content tile-content-forms">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><bean:message key="pluginmanager.plugin.id" /></label>
            </div>
            <div class="col-sm-8">
                <c:set var="pluginIdValue" value="${pluginForm.id}"/>
                <c:if test="${pluginForm.system}">
                    <c:set var="pluginIdValue">
                        ${pluginIdValue} (<bean:message key="pluginmanager.plugin.systemplugin" />)
                    </c:set>
                </c:if>
                <input type="text" class="form-control" readonly="readonly" value="${pluginIdValue}"/>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><bean:message key="pluginmanager.plugin.name" /></label>
            </div>
            <div class="col-sm-8">
                <input type="text" class="form-control" readonly value="${pluginForm.name}"/>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><bean:message key="Description" /></label>
            </div>
            <div class="col-sm-8">
                <input class="form-control" type="text" readonly value="${pluginForm.description}"/>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><bean:message key="pluginmanager.plugin.version" /></label>
            </div>
            <div class="col-sm-8">
                <input type="text" class="form-control" readonly value="${pluginForm.version}"/>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><bean:message key="pluginmanager.plugin.vendor" /></label>
            </div>
            <div class="col-sm-8">
                <input type="text" class="form-control" readonly value="${pluginForm.vendor}"/>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><bean:message key="pluginmanager.plugin.status" /></label>
            </div>
            <div class="col-sm-8">
                <c:set var="activeMsgKey" value="${pluginForm.active ? 'default.status.active' : 'pluginmanager.plugin.deactivated'}"/>
                <input type="text" class="form-control" readonly="readonly" value="<bean:message key="${activeMsgKey}"/>">
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><bean:message key="pluginmanager.plugin.depending_plugins"/></label>
            </div>
            <div class="col-sm-8">
                <c:choose>
                    <c:when test="${not empty pluginForm.dependingPluginIds}">
                        <div class="list-group">
                            <c:forEach items="${pluginForm.dependingPluginIds}" var="dependingId">
                                <c:url var="dependingPluginLink" value="/administration/pluginmanager/plugin/${dependingId}/view.action"/>
                                <a href="${dependingPluginLink}" class="list-group-item">${dependingId}</a>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <input type="text" class="form-control" readonly="readonly" value="<bean:message key="pluginmanager.plugin.no_depending_plugins"/>"/>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </div>

</div>
