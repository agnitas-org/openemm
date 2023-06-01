(function () {

    function readActualToken() {
        if (isProtectionEnabled()) {
            return AGN.Lib.Storage.readCookie(window.csrfCookieName);
        }

        return '';
    }

    function isProtectionEnabled() {
        return window.csrfHeaderName && window.csrfParameterName && window.csrfCookieName;
    }

    function setTokenToReqHeader(jqxhr, reqType) {
        if (isProtectionEnabled() && reqType !== 'GET') {
            const csrfToken = readActualToken();
            jqxhr.setRequestHeader(window.csrfHeaderName, csrfToken);
        }
    }

    function updateTokenInDOM($scope, needsHtmlInResult) {
        if (!isProtectionEnabled()) {
            return $scope;
        }

        if (!$scope) {
            $scope = $(document);
        } else if (!($scope instanceof $)) {
            $scope = $('<div>').append($scope);
        }

        const csrfToken = readActualToken();
        const $tokenInputs = $scope.find('input[name="' + window.csrfParameterName + '"]');

        _.each($tokenInputs, function (input) {
            $(input).val(csrfToken);
        });

        if (needsHtmlInResult) {
            return $scope.html();
        }
    }

    AGN.Lib.CSRF = {
        readActualToken: readActualToken,
        isProtectionEnabled: isProtectionEnabled,
        setTokenToReqHeader: setTokenToReqHeader,
        updateTokenInDOM: updateTokenInDOM
    };
})();
