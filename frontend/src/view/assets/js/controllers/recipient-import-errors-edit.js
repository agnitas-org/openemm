AGN.Lib.Controller.new('recipient-import-errors-edit', function () {
    const Page = AGN.Lib.Page;

    this.addDomInitializer('recipient-import-errors-edit', function() {
        if (isCurrentPageReloaded()) {
            Page.reload(AGN.url('/recipient/import/chooseMethod.action?cancelImport=true'))
        } else {
            window.onbeforeunload = function () {
                return "If you leave the page, the import will be canceled automatically!";
            }
        }
    });

    function isCurrentPageReloaded() {
        if (typeof performance === 'undefined' || typeof performance.getEntriesByType !== 'function') {
            return false;
        }

        const entries = performance.getEntriesByType('navigation');
        if (entries.length > 0) {
            return entries[0].type === 'reload' && entries[0].name === window.location.href;
        }

        return false;
    }

    this.addAction({
        submission: 'save-errors'
    }, function () {
        unbindUnloadEvents();

        const form = AGN.Lib.Form.get(this.el);
        form.submit('static');
    });

    this.addAction({
        click: 'ignore-errors'
    }, function () {
        unbindUnloadEvents();
        Page.reload(AGN.url('/recipient/import/errors/ignore.action'));
    });

    this.addAction({
        click: 'cancel'
    }, function () {
        unbindUnloadEvents();
        Page.reload(AGN.url('/recipient/import/cancel.action'));
    });

    function unbindUnloadEvents() {
        window.onbeforeunload = null;
    }
});
