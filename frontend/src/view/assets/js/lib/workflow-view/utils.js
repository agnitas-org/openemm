(function() {
    var Def = AGN.Lib.WM.Definitions,
        Helpers = AGN.Lib.Helpers;

    AGN.Lib.WM.Utils = {
        checkActivation: function() {
            var currentWorkflowStatus = $('#workflow-status').val();

            if (Def.constants.initialWorkflowStatus === Def.constants.statusActive &&
                (currentWorkflowStatus === Def.constants.statusActive || currentWorkflowStatus === Def.constants.statusTesting)) {
                AGN.Lib.Messages(t('workflow.defaults.error'), t('error.workflow.saveActivatedWorkflow'), 'alert');
                return true;
            } else {
                return false;
            }
        },

        getConfigData: function($e) {
            return Helpers.objFromString($e.data('config'));
        },


        mapAsOptionsHtml: function(options) {
            var html = '';

            Object.keys(options).forEach(function(key) {
                var attributes = 'value="' + key + '"';

                html += '<option ' + attributes + '>' + options[key] + '</option>';
            });

            return html;
        },

        arrayAsOptionsHtml: function(options) {
            var html = '';

            options.forEach(function(option) {
                var attributes = 'value="' + option.id + '"';

                var extras = option.data;
                if (extras) {
                    Object.keys(extras).forEach(function(k) {
                        attributes += ' data-' + k + '="' + extras[k].replace('"', '&quot;') + '"';
                    });
                }

                var text = option.text
                    .replace('<', '&lt;')
                    .replace('>', '&gt;');

                html += '<option ' + attributes + '>' + text + '</option>';
            });

            return html;
        }
    };
})();
