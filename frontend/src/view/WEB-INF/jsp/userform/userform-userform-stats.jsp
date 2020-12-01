<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="birtStatisticUrlWithoutFormat" type="java.lang.String"--%>

<div class="row">
    <div class="col-xs-12">

        <div class="tile">
            <div class="tile-header">
                <div class="headline">
                    <mvc:message code="Statistics"/>
                </div>
                <ul class="tile-header-actions">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-cloud-download"></i>
                            <span class="text">
                                <mvc:message code="Export"/>
                            </span>
                            <i class="icon icon-caret-down"></i>
                        </a>
                        <ul class="dropdown-menu">
                            <li class="dropdown-header">
                                <mvc:message code="statistics.exportFormats"/>
                            </li>
                            <li>
                                <a tabindex="-1" href="${birtStatisticUrlWithoutFormat}&__format=csv" data-prevent-load="">
                                    <i class="icon icon-file-excel-o"></i>
                                    <mvc:message code="export.message.csv"/>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-content">
                <iframe src="${birtStatisticUrlWithoutFormat}&__format=html" border="0" scrolling="auto" style="width: 100%" frameborder="0">
                    Your Browser does not support IFRAMEs, please update!
                </iframe>
            </div>
        </div>

    </div>
</div>
