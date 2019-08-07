(function () {
    var expiresDays = 30;

    var saveDate = function () {
        localStorage.setItem('closedDate', new Date());
    };

    var isExpired = function (expiresDays) {
        var currentDate = new Date();
        var requiredDate = new Date(localStorage.getItem('closedDate'));
        requiredDate.setDate(requiredDate.getDate() + parseInt(expiresDays));

        if (currentDate - requiredDate > 0) {
            return true;
        }
        return false;
    };

    AGN.Lib.DomInitializer.new('logon-notification', function ($e) {
        var head = t('defaults.info');
        var content = t('logon.info.multiple_tabs');

        if (isExpired(expiresDays)) {
            AGN.Lib.Messages(head, content, 'info', saveDate, false);
        }
    });

})();