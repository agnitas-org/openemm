(function() {

    var MailingSelectorBaseNew = function(form, container, pageContextSessionId, selectName, noMailingOption, editorsHelper) {

        this.formNameJId = "form[name='" + form + "']";
        this.container = container;
        this.pageContextSessionId = pageContextSessionId;
        this.selectNameJId = "select[name=" + selectName + "]";
        this.defaultSortField = "date";
        this.mailingId = 0;
        this.noMailingOption = noMailingOption;
        this.editorsHelper = editorsHelper;

        this.cleanOptions = function(defaultSortOrder) {
            if (defaultSortOrder == undefined) {
                defaultSortOrder = 0;
            }
            jQuery(this.container + " .sort").prop("press", defaultSortOrder);
            this.onMailingSortClick(this.defaultSortField);
        };

        this.onMailingSelectChange = function(value) {
        };

        this.onMailingSortClick = function(field) {
            jQuery(this.container + " .arrowUp").hide();
            jQuery(this.container + " .arrowDown").hide();
            var sortElements = jQuery(this.container + " .sort");
            var sortId = this.container + " #" + field + "_sort";
            var sortElement = jQuery(sortId);
            var arrowUpElement = jQuery(sortId + " .arrowUp");
            var arrowDownElement = jQuery(sortId + " .arrowDown");
            var press = parseInt(sortElement.prop("press"));
            var sortDirection = "asc";
            sortElements.prop("press", 0);
            switch (press) {
                case 0 :
                    sortElement.prop("press", 1);
                    arrowUpElement.show();
                    sortDirection = "asc";
                    break;
                case 1 :
                    sortElement.attr("press", 2);
                    arrowDownElement.show();
                    sortDirection = "desc";
                    break;
                case 2 :
                    sortElement.prop("press", 1);
                    arrowUpElement.show();
                    sortDirection = "asc";
                    break;
            }

            this.getAllMailingSorted(field, sortDirection);
        };

        this.setMailingId = function(mailingId) {
            this.mailingId = mailingId;
        };

        this.getAllMailingSorted = function(field, direction) {
            var self = this;
            var mailingsList = jQuery(this.formNameJId + " " + this.selectNameJId);
            var mailingsListValue = mailingsList.val();
            if (mailingsListValue != null) {
                self.mailingId = mailingsListValue;
            }
            mailingsList.attr("readonly", "readonly");
            jQuery.ajax({
                type: "POST",
                url: AGN.url("/workflow/getAllMailingSorted.action"),
                data: {
                    sortField: field,
                    sortDirection: direction
                },
                success: function(data) {
                    mailingsList.html("");
                    if (self.noMailingOption) {
                        mailingsList.append('<option value="0">' + t('workflow.defaults.no_mailing') + '</option>');
                    }
                    for(var i = 0; i < data.length; i++) {
                        mailingsList.append("<option " + " value='" + data[i].mailingID + "'>" + data[i].shortname + "</option>");
                    }
                    mailingsList.removeAttr("readonly");
                    if (self.mailingId == 0 && self.noMailingOption == false && data.length > 0) {
                        mailingsList.val(data[0].mailingID);
                    } else {
                        mailingsList.val(self.mailingId);
                    }
                    editorsHelper.initSelectWithValueOrChooseFirst(mailingsList, mailingsList.val());
                }
            });
        };

        this.getSelectedMailingOption = function() {
            return jQuery(this.formNameJId + " " + this.selectNameJId + " option[value=" + this.mailingId + "]");
        };
    };

    AGN.Lib.WM.MailingSelectorBaseNew = MailingSelectorBaseNew;

})();
