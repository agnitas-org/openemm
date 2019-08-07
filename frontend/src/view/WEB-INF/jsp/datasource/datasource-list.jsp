<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>

<%@ taglib prefix="agn"     uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html"    uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="datasources" type="org.agnitas.beans.impl.PaginatedListImpl<com.agnitas.emm.core.datasource.bean.DataSource>"--%>
<%--@elvariable id="datasourceForm" type="com.agnitas.emm.core.datasource.form.DatasourceForm"--%>

<mvc:form servletRelativeAction="/importexport/datasource/list.action"
          id="datasourceForm"
          modelAttribute="datasourceForm"
          data-form="resource">
    <mvc:hidden path="page"/>
    <mvc:hidden path="dir"/>
    <mvc:hidden path="order"/>
    <mvc:hidden path="sort"/>
    
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "datasource-overview": {
                "rows-count": ${datasourceForm.numberOfRows}
            }
        }
    </script>
    
	<div class="tile">
    	<div class="tile-header">
        	<h2 class="headline"><bean:message key="default.Overview"/></h2>
        	<ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><bean:message key="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><bean:message key="listSize"/></li>
                        <li>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
    	</div>

    	<div class="tile-content">
        	<div class="table-wrapper">
            	<display:table id="datasource"
                               name="datasources"
                               requestURI="/importexport/datasource/list.action"
                               class="table table-bordered table-striped table-hover js-table"
                               sort="external"
                               partialList="true"
                               size="${datasources.fullListSize}"
                               pagesize="${datasourceForm.numberOfRows gt 0 ? datasourceForm.numberOfRows : 20}"
                               excludedParams="*">
                	<display:column titleKey="recipient.DatasourceId"
                                    property="id"
                                    headerClass="js-checkable"
                                    sortable="true"
                                    sortProperty="datasource_id"/>

                	<display:column titleKey="Description"
                                    property="description"
                                    headerClass="js-checkable"
                                    sortable="true"
                                    sortProperty="description"/>
                    <display:caption>
						<div class="l-tile-recipient-info-box align-left">
                        	<span> <bean:message key="recipient.datasource.info"/></span>
						</div>
                    </display:caption>
            	</display:table>
        	</div>
    	</div>
	</div>
</mvc:form>
