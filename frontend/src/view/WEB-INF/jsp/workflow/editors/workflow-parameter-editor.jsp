<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div id="parameter-editor" data-initializer="parameter-editor-initializer" >
    <mvc:form action="" id="parameterForm" name="parameterForm">
        <input name="id" type="hidden">

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label">
                    <bean:message key="Value"/>
                </label>
            </div>
            <div class="col-sm-8">
                <select name="value" class="form-control">
                    <option value="5">5</option>
                    <option value="10">10</option>
                    <option value="15">15</option>
                    <option value="20">20</option>
                    <option value="25">25</option>
                    <option value="30">30</option>
                    <option value="33">33</option>
                    <option value="40">40</option>
                    <option value="50">50</option>
                    <option value="60">60</option>
                    <option value="70">70</option>
                    <option value="75">75</option>
                    <option value="80">80</option>
                    <option value="85">85</option>
                    <option value="90">90</option>
                </select>
            </div>
        </div>

        <hr>

        <div class="form-group">
            <div class="col-xs-12">
                <div class="btn-group">
                    <a href="#" class="btn btn-regular" data-action="editor-cancel">
                        <bean:message key="button.Cancel"/>
                    </a>

                    <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="parameter-editor-save">
                        <bean:message key="button.Apply"/>
                    </a>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
