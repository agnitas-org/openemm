(function() {
    const EditorsHelper = AGN.Lib.WM.EditorsHelper;

    AGN.Lib.WM.MailingSelector = function(form, selectName, noMailingOption) {
        this.formNameJId = 'form[name="' + form + '"]';
        this.selectNameJId = 'select[name=' + selectName + ']';
        this.defaultSortField = 'date';
        this.mailingId = 0;
        this.noMailingOption = noMailingOption;

        this.cleanOptions = function() {
            this.getAllMailingSorted(this.defaultSortField, 'asc');
        };

        this.onMailingSelectChange = function(value) {
        };

        this.setMailingId = function(mailingId) {
            this.mailingId = mailingId;
        };

        this.getAllMailingSorted = function(field, direction) {
            var self = this;
            var mailingsList = $(this.formNameJId + ' ' + this.selectNameJId);
            var mailingsListValue = mailingsList.val();
            if (mailingsListValue != null) {
                self.mailingId = mailingsListValue;
            }
            mailingsList.attr('readonly', 'readonly');
            $.ajax({
                type: 'GET',
                url: AGN.url('/workflow/getAllMailingSorted.action'),
                data: {
                    sortField: field,
                    sortDirection: direction
                },
                success: function(data) {
                    mailingsList.html('');
                    if (self.noMailingOption) {
                        mailingsList.append('<option value="0">' + t('workflow.defaults.no_mailing') + '</option>');
                    }
                    for (var i = 0; i < data.length; i++) {
                        mailingsList.append('<option ' + ' value="' + data[i].mailingID + '">' + data[i].shortname + '</option>');
                    }
                    mailingsList.removeAttr('readonly');
                    if (self.mailingId == 0 && self.noMailingOption == false && data.length > 0) {
                        mailingsList.val(data[0].mailingID);
                    } else {
                        mailingsList.val(self.mailingId);
                    }
                    EditorsHelper.initSelectWithValueOrChooseFirst(mailingsList, mailingsList.val());
                }
            });
        };

        this.getSelectedMailingOption = function() {
            return $(this.formNameJId + ' ' + this.selectNameJId + ' option[value=' + this.mailingId + ']');
        };
    };
})();
