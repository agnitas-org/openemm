AGN.Lib.Controller.new('company-tech-emails', function() {

    var scope = this.el;
    var rowSelector = '';
    var targetFieldName = '';

    var dataList = function() {
        var result = [""];
        var stringValue = $('input[name="' + targetFieldName + '"]').val();
        if(stringValue) {
            result = stringValue.split(', ');
        }
        return result;
    };

    var rowValue = function() {
        var values = [];
        _.each(scope.find(rowSelector + " input"), function(rowInput) {
            var value = rowInput.value;
            if(value.trim() !== '') {
                values.push(value);
            }
        });
        return values;
    };

    var removeDeleteBtn = function() {
        var $rows = scope.find(rowSelector);
        if($rows.length === 1 && $rows.find('.btn-alert')) {
            $rows.find('.btn-alert').remove();
        }
    };

    var addDeleteBtns = function() {
        _.each(scope.find(rowSelector), function(row) {
            var $row = $(row);
            if($row.find('.btn-alert').length === 0) {
                $row.find('.input-group-btn').append(AGN.Lib.Template.text('remove-button', {}));
            }
        });
    };

    var addFieldRow = function(el, value, setAfter) {
        var template = AGN.Lib.Template.text('email-input', {email: value});
        if(setAfter) {
            el.closest(rowSelector).after(template);
        } else {
            el.append(template);
        }
        addDeleteBtns();
    };

    var saveDataToField = function() {
        $('input[name="' + targetFieldName + '"]').val(rowValue().join(', '));
    };

    this.addDomInitializer('company-emails-list', function() {
        scope = this.el;
        targetFieldName =  scope.data('target-field') || '';
        rowSelector = scope.data('row-selector') || '';

        _.each(dataList(), function (email) {
            addFieldRow(scope, email, false);
        });

        removeDeleteBtn();
    });

    this.addAction({'click': 'deleteContactEmail'}, function(){
        this.el.closest(rowSelector).remove();
        removeDeleteBtn();
        saveDataToField();
    });

    this.addAction({'click': 'createContactEmail'}, function() {
        addFieldRow(this.el, "", true);
        saveDataToField();
    });

    this.addAction({'change': 'inputValue'}, function() {
        saveDataToField();
    });
});