(function() {
    var Def = AGN.Lib.WM.Definitions,
        Confirm = AGN.Lib.Confirm;

    var Dialogs = {
        Activation: activationDialog,
        Deactivation: deactivationDialog,
        confirmTestingStartStop: confirmTestingStartStop,
        confirmMailingDataTransfer: confirmMailingDataTransfer,
        confirmOwnWorkflowExpanding: confirmOwnWorkflowExpanding,
        confirmCopy: confirmCopy
    };

    function activationDialog(onSuccess, mailingNames, isUnpause) {
        var $activatingDialog = $('#activating-campaign-dialog');
        if (!isUnpause) {
          $('#activating-campaign-mailings').html(mailingNames);
        }
        $activatingDialog.css('visibility', 'visible');
        $activatingDialog.show();
        var activateBtn = $('#activating-campaign-activate-button');
        activateBtn.off('click');
        activateBtn.on('click', function() {
            onSuccess();
            $activatingDialog.dialog('close');
            return false;
        });
        $activatingDialog.dialog({
            open: function() {
                var title = $activatingDialog.parent().find('.ui-dialog-title');
                title.empty();
                title.append('<span class="dialog-title-image">' + t(isUnpause ? 'workflow.activating.unpauseTitle' : 'workflow.activating.title') + '</span>');
                title.find('.dialog-title-image').css('padding-left', '0px');
            },
            modal: true,
            resizable: false,
            width: 650,
            minHeight: 0,
            close: function() {
                return false;
            }
        });
    }

    function deactivationDialog(onSuccess) {
        var $inactivatingDialog = $('#inactivating-campaign-dialog');
        $inactivatingDialog.css('visibility', 'visible');
        $inactivatingDialog.show();
        var deactivatingBtn = $('#inactivating-campaign-inactivate-button');
        deactivatingBtn.off('click');
        deactivatingBtn.on('click', function() {
            onSuccess();
            $inactivatingDialog.dialog('close');
            return false;
        });
        $inactivatingDialog.dialog({
            open: function() {
                var title = $inactivatingDialog.parent().find('.ui-dialog-title');
                title.empty();
                title.append('<span class="dialog-title-image">' + t('workflow.inactivating.title') + '</span>');
                title.find('.dialog-title-image').css('padding-left', '0px');
            },
            modal: true,
            resizable: false,
            width: 650,
            minHeight: 0,
            close: function() {
                return false;
            }
        });
    }

    function confirmMailingDataTransfer(paramsToAsk) {
        return Confirm.createFromTemplate({Def: Def, paramsToAsk: paramsToAsk}, 'mailing-data-transfer-modal');
    }

    function confirmOwnWorkflowExpanding() {
        return Confirm.createFromTemplate({}, 'own-workflow-expanding-modal');
    }

    function confirmTestingStartStop(isStartTesting) {
        return Confirm.createFromTemplate({startTesting: isStartTesting, shortname: Def.shortname}, 'testing-modal');
    }

    function confirmCopy(hasContent) {
        return Confirm.createFromTemplate({hasContent: hasContent === true}, 'workflow-copy-modal');
    }

    AGN.Lib.WM.Dialogs = Dialogs;
})();
