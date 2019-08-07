<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="tile-header">
    <h2 class="headline"><bean:message key="default.Overview"/></h2>
    <ul class="tile-header-actions">
        <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                <i class="icon icon-cloud-download"></i>
                <span class="text"><bean:message key="Export"/></span>
                <i class="icon icon-caret-down"></i>
            </a>
            <ul class="dropdown-menu">
                <li class="dropdown-header">
                    <bean:message key="statistics.exportFormats"/>
                </li>
                <li>
                    <a href="#" tabindex="-1" data-form-set="reportFormat:csv" data-form-submit-static><i class="icon icon-file-excel-o"></i> <bean:message key="user.export.csv"/></a>
                    <%--<a href="#" tabindex="-1" data-form-set="reportFormat:pdf" data-form-submit-static><i class="icon icon-file-pdf-o"></i> <bean:message key="user.export.pdf"/></a>--%>
                </li>
            </ul>
        </li>
        <c:if test="${param.isCompareBtnShow}">
            <li>
                <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                    <i class="icon icon-search"></i>
                    <span class="text"><bean:message key="statistic.compare"/></span>
                </button>
            </li>
        </c:if>
    </ul>
</div>
