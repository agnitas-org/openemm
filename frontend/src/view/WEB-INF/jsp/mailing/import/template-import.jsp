<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<c:url var="action" value="/import/execute.action"/>
<mvc:form action="${action}" id="mailingImport" modelAttribute="form" enctype="multipart/form-data" data-form="resource">
	<mvc:hidden path="template" value="true"/>
	<mvc:hidden path="grid" />

	<div class="tile">
		<div class="tile-header">
			<h2 class="headline">
				<mvc:message code="template.import" />
			</h2>
		</div>
		<div class="tile-content">
			<div class="tile-content-forms form-vertical">
				<div class="form-group">
					<div class="input-group">
						<div class="input-group-controls">
							<input type="file" name="uploadFile" id="uploadFile" class="form-control"/>
						</div>
						<div class="input-group-btn">
							<button type="button" class="btn btn-regular btn-primary" data-form-submit>
								<i class="icon icon-cloud-upload"></i>
								<span class="text">
									<mvc:message code="template.import"/>
								</span>
							</button>
						</div>
					</div>
				</div>
                <emm:ShowByPermission token="settings.extended">
                    <div class="col-sm-4">
                        <table>
                            <tr>
                                <td>
                                    <label data-form-change class="toggle">
                                        <mvc:checkbox id="import_duplicates" path="overwriteTemplate"/>
                                        <div class="toggle-control"></div>
                                    </label>
                                </td>
                                <td>
                                    <label class="control-label" style="margin-left: 7px;">
                                        <mvc:message code="import.template.overwrite" />
                                        <button type="button" class="icon icon-help" data-help="help_${helplanguage}/mailing/OverwriteTemplate.xml"></button>
                                    </label>
                                </td>
                            </tr>
                        </table>
                    </div>
                </emm:ShowByPermission>
			</div>
		</div>
	</div>
</mvc:form>
