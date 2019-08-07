<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.layoutbuilder.forms.TemplatesStyle" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="AREA_BACKGROUND_COLOR" value="<%= TemplatesStyle.AREA_BACKGROUND.getStringStyleKey() %>"/>
<c:set var="AREA_DIV_BACKGROUND_COLOR" value="<%= TemplatesStyle.AREA_BACKGROUND_IN_DIV.getStringStyleKey() %>"/>
<c:set var="AREA_FONT_COLOR" value="<%= TemplatesStyle.AREA_FONT_COLOR.getStringStyleKey() %>"/>
<c:set var="AREA_GRID_COLOR" value="<%= TemplatesStyle.AREA_GRID_COLOR.getStringStyleKey() %>"/>

<emm:CheckLogon/>

<%@include file="/WEB-INF/jsp/messages.jsp" %>

<logic:notEmpty name="mailingSendForm" property="styles">
    <c:set var="styles" value="${mailingSendForm.styles}"/>

    <style type="text/css">
        <c:if test="${not empty styles[AREA_BACKGROUND_COLOR]}">
        body {
            background-color: ${styles[AREA_BACKGROUND_COLOR]} !important;
        }
        </c:if>

        <c:if test="${not empty styles[AREA_DIV_BACKGROUND_COLOR] or not empty styles[AREA_FONT_COLOR]}">
        .div-container {
            <c:if test="${not empty styles[AREA_DIV_BACKGROUND_COLOR]}">
            background-color: ${styles[AREA_DIV_BACKGROUND_COLOR]} !important;
            </c:if>

            <c:if test="${not empty styles[AREA_FONT_COLOR]}">
            color: ${styles[AREA_FONT_COLOR]} !important;
            </c:if>
        }
        </c:if>
    </style>
</logic:notEmpty>

<bean:write name="mailingSendForm" property="preview" filter="false"/>
