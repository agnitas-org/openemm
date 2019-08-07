<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do"%>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="companyID" value="<%= AgnUtils.getCompanyID(request) %>"/>
<c:set var="formID" value="${trackableUserFormLinkStatForm.formID}"/>

<div class="row">
    <div class="col-xs-12">

        <div class="tile">
            <div class="tile-header">
                <div class="headline">
                    <bean:message key="Statistics"/>
                </div>
                <ul class="tile-header-actions">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-cloud-download"></i>
                            <span class="text">
                                <bean:message key="Export"/>
                            </span>
                            <i class="icon icon-caret-down"></i>
                        </a>
                        <ul class="dropdown-menu">
                            <li class="dropdown-header">
                                <bean:message key="statistics.exportFormats"/>
                            </li>
                            <li>
                                <a target="_blank" tabindex="-1" href="${birturl}/run?__report=formula_click_stat.rptdesign&companyID=${companyID}&emmsession=${emmsession}&formID=${formID}&targetbaseurl=${targetbaseurl}&uid=${uid}&__format=csv&language=${language}">
                                    <i class="icon icon-file-excel-o"></i>
                                    <bean:message key="export.message.csv"/>
                                </a>
                                <%--<a target="_blank" tabindex="-1" href="${birturl}/run?__report=formula_click_stat.rptdesign&companyID=${companyID}&emmsession=${emmsession}&formID=${formID}&targetbaseurl=${targetbaseurl}&uid=${uid}&__format=pdf&language=${language}">--%>
                                    <%--<i class="icon icon-file-pdf-o"></i>--%>
                                    <%--<bean:message key="export.message.pdf"/>--%>
                                <%--</a>--%>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-content">
                <iframe src="${birturl}/run?__report=formula_click_stat.rptdesign&companyID=${companyID}&language=${language}&emmsession=${emmsession}&formID=${formID}&targetbaseurl=${targetbaseurl}&uid=${uid}"
                        border="0" frameborder="0" scrolling="auto" width="100%" >
                    Your Browser does not support IFRAMEs, please update!
                </iframe>
            </div>
        </div>

    </div>
</div>
