//customize jquery-dirty plugin
(function($) {

    $.extend(true, $.fn.dirty.prototype, {
        checkValues: function(e) {
            var d = this;
            var formIsDirty = false;

            this.form.find("select").each(function(_, el) {
                var $el = $(el);
                var thisIsDirty = d.isSelectFieldDirty($el);

                $el.data(d.statuses.dataIsDirty, thisIsDirty);

                formIsDirty |= thisIsDirty;
            });

            this.form.find("input, textarea").each(function(_, el) {
                var isRadioOrCheckbox = d.isRadioOrCheckbox(el);
                var isFile = d.isFileInput(el);
                var $el = $(el);

                var thisIsDirty;
                if (isRadioOrCheckbox) {
                    thisIsDirty = d.isCheckboxDirty($el);
                } else if (isFile) {
                    thisIsDirty = d.isFileInputDirty($el);
                } else {
                    if (!$el.is('.select2-input[placeholder]')) {
                        thisIsDirty = d.isFieldDirty($el);
                    }
                }

                $el.data(d.statuses.dataIsDirty, thisIsDirty);

                formIsDirty |= thisIsDirty;
            });

            if (formIsDirty) {
                d.setDirty();
            } else {
                d.setClean();
            }
        },

        saveInitialValues: function() {
            var d = this;
            this.form.find("input, select, textarea").each(function(uid, e) {
                var $el = $(e);
                if ($el.attr('id') || $el.attr('name')) {
                    $el.attr('id', $el.prop('tagName' + '__' + uid));
                }

                var isRadioOrCheckbox = d.isRadioOrCheckbox(e);
                var isFile = d.isFileInput(e);

                if (isRadioOrCheckbox) {
                    var isChecked = $(e).is(":checked") ? "checked" : "unchecked";
                    $el.data(d.statuses.dataInitialValue, isChecked);
                } else if(isFile){
                    $el.data(d.statuses.dataInitialValue, JSON.stringify(e.files))
                } else {
                    $el.data(d.statuses.dataInitialValue, $el.val() || '');
                }
            });
        },

        backupData: function() {
            var d = this;
            var json = {};

            d.form.find("input, select, textarea").each(function(_, el) {
                var $el = $(el);

                var id = $el.attr('id') || $el.attr('name');

                var data = {};
                data[d.statuses.dataInitialValue] = $el.data(d.statuses.dataInitialValue);
                data[d.statuses.dataIsDirty] = $el.data(d.statuses.dataIsDirty);
                json[id] = data;
            });

            return json;
        },

        restoreData: function(json) {
            var d = this;
            if (json) {
                d.form.find("input, select, textarea").each(function(_, el) {
                    var $el = $(el);
                    var id = $el.attr('id') || $el.attr('name');

                    var data = json[id];
                    if (data) {
                        $el.data(d.statuses.dataInitialValue, data[d.statuses.dataInitialValue]);
                        $el.data(d.statuses.dataIsDirty, data[d.statuses.dataIsDirty]);
                    }
                });
            }

            d.checkValues();
        },

        isSelectFieldDirty: function($field) {
            var d = this;
            var initialValue = $field.data(d.statuses.dataInitialValue) || '';
            var currentValue = $field.val() || '';

            return !_.isEqual(currentValue, initialValue);
        },

        resetForm: function() {
            var d = this;

            this.form.find("select").each(function(_, e) {

                var $e = $(e);

                var value = $e.data(d.statuses.dataInitialValue);
                if ($e.data('select2')) {
                    $e.select2('val', value);
                } else {
                    $e.val(value);
                }
            });

            this.form.find("input, textarea").each(function(_, e) {
                var $e = $(e);
                var isRadioOrCheckbox = d.isRadioOrCheckbox(e);
                var isFile = d.isFileInput(e);

                if (isRadioOrCheckbox) {
                    var initialCheckedState = $e.data(d.statuses.dataInitialValue);
                    var isChecked = initialCheckedState === "checked";

                    $e.prop("checked", isChecked);
                } if (isFile) {
                    e.value = "";
                    $(e).data(d.statuses.dataInitialValue, JSON.stringify(e.files))
                } else {
                    var value = $e.data(d.statuses.dataInitialValue);
                    $e.val(value);
                }
            });

            this.checkValues();
        }

    });
})(jQuery);