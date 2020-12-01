(function(){

    AGN.Lib.DomInitializer.new('recipient-report-initializer', function($el) {
       initLogIframeBackground($el);
    });

    function initLogIframeBackground($scope) {
        if(!$('body').hasClass('dark-theme')) {
            return;
        }
        var $iframe = $scope.find('iframe');
        $iframe.on('load.iframe', function() { changeColorsToDarkTheme($iframe); });
        $iframe.ready(function () { changeColorsToDarkTheme($iframe); })
    }

    function changeColorsToDarkTheme($iframe) {
        var $body = $iframe.contents().find('body');
        var $center = $body.find('center');
        if($center.length) {
            $body.css('background', '#303030');
            $center.css('background', '#303030');//$c-dt-tile-background = #303030
        } else {
            $iframe.contents().find('body').css('color', '#ffffff');
        }
    }

})();