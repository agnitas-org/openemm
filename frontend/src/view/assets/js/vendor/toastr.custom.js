/* modified Toastr
 * original credits below

 * Toastr
 * Copyright 2012-2014 John Papa and Hans Fjällemark.
 * All Rights Reserved.
 * Use, reproduction, distribution, and modification of this code is subject to the terms and
 * conditions of the MIT license, available at http://www.opensource.org/licenses/mit-license.php
 *
 * Author: John Papa and Hans Fjällemark
 * ARIA Support: Greta Krafsig
 * Project: https://github.com/CodeSeven/toastr
 */
; (function (define) {
    define(['jquery', 'lodash'], function ($, _) {
        return (function () {
            var $container;
            var listener;
            var toastId = 0;
            var toastType = {
                alert: 'alert',
                info: 'info',
                success: 'success',
                warning: 'warning'
            };

            var toastr = {
                clear: clear,
                remove: remove,
                alert: alert,
                getContainer: getContainer,
                info: info,
                options: {},
                subscribe: subscribe,
                success: success,
                version: '2.0.3',
                warning: warning,
                warning_permanent: warning
            };

            return toastr;

            //#region Accessible Methods

            function alert(title, message, options) {
                return notify({
                    type: toastType.alert,
                    title: title,
                    message: message,
                    options: options
                });
            }

            function info(title, message, options) {
                return notify({
                    type: toastType.info,
                    title: title,
                    message: message,
                    options: options
                });
            }

            function success(title, message, options) {
                return notify({
                    type: toastType.success,
                    title: title,
                    message: message,
                    options: options
                });
            }

            function warning(title, message, options) {
                return notify({
                    type: toastType.warning,
                    title: title,
                    message: message,
                    options: options
                });
            }

            function subscribe(callback) {
                listener = callback;
            }

            function getContainer(options, create) {
                if (!options) { options = getOptions(); }
                $container = $('#' + options.containerId);
                if ($container.length) {
                    return $container;
                }
                if(create) {
                    $container = createContainer(options);
                }
                return $container;
            }

            function clear($toastElement) {
                var options = getOptions();
                if (!$container) { getContainer(options); }
                if (!clearToast($toastElement, options)) {
                    clearContainer(options);
                }
            }

            function remove($toastElement) {
                var options = getOptions();
                if (!$container) { getContainer(options); }
                if ($toastElement && $(':focus', $toastElement).length === 0) {
                    removeToast($toastElement);
                    return;
                }
                if ($container.children().length) {
                    $container.remove();
                }
            }
            //#endregion

            //#region Internal Methods

            function clearContainer(options){
                var toastsToClear = $container.children();
                for (var i = toastsToClear.length - 1; i >= 0; i--) {
                    clearToast($(toastsToClear[i]), options);
                };
            }

            function clearToast($toastElement, options){
                if ($toastElement && $(':focus', $toastElement).length === 0) {
                    $toastElement[options.hideMethod]({
                        duration: options.hideDuration,
                        easing: options.hideEasing,
                        complete: function () { removeToast($toastElement); }
                    });
                    return true;
                }
                return false;
            }

            function createContainer(options) {
                $container = $('<div/>')
                    .attr('id', options.containerId)
                    .addClass(options.positionClass)
                    .attr('aria-live', 'polite')
                    .attr('role', 'alert');

                $container.appendTo($(options.target));
                return $container;
            }

            function getDefaults() {
                return {
                    tapToDismiss: false,
                    toastClass: 'notification',
                    containerId: 'notifications-container',
                    debug: false,

                    showMethod: 'fadeIn', //fadeIn, slideDown, and show are built into jQuery
                    showDuration: 300,
                    showEasing: 'swing', //swing and linear are built into jQuery
                    onShown: undefined,
                    hideMethod: 'fadeOut',
                    hideDuration: 1000,
                    hideEasing: 'swing',
                    onHidden: undefined,

                    extendedTimeOut: 1000,
                    onCloseClick: undefined,
                    positionClass: 'notification-below-header',
                    timeOut: 5000, // Set timeOut and extendedTimeout to 0 to make it sticky
                    titleClass: 'headline',
                    messageClass: 'notification-content',
                    closeIcon: 'icon icon-times-circle',
                    target: 'body',
                    newestOnTop: true
                };
            }

            function publish(args) {
                if (!listener) { return; }
                listener(args);
            }

            function getTemplate() {
              return this._template || (this._template = 
                '<div class="{{= toastClass }} {{= toastClass }}-{{= type }}">' +
                  '<div class="notification-header">' +
                    '<p class="{{= titleClass }}">' +
                      '<i class="icon icon-state-{{= type }}"></i>&nbsp;' +
                      '<span class="text">{{= title }}</span>' +
                      '<i class="js-close close-icon {{= closeIcon }}"></i>' +
                    '</p>' +
                  '</div>' +
                  '<div class="{{= messageClass }}" style="display: none;">{{= message }}</div>' +
                '</div>'
              );
            }

            function notify(map) {
              var globalOptions = getOptions(),
                  options = $.extend(globalOptions, map);

                if (typeof map.options !== 'undefined') {
                  options = $.extend(options, map.options);
                }

                toastId++;

                $container = getContainer(options, true);

                var template = _.template(getTemplate())(options),
                    $template = $(template);


                var intervalId = null,
                    $toastElement = $template,
                    $closeElement = $toastElement.find('.js-close'),
                    response = {
                        toastId: toastId,
                        state: 'visible',
                        startTime: new Date(),
                        options: options
                    };

                if (options.message) {
                  $toastElement.find('.' + options.messageClass).show();
                }

                $toastElement.hide();
                if (options.newestOnTop) {
                    $container.prepend($toastElement);
                } else {
                    $container.append($toastElement);
                }


                $toastElement[options.showMethod](
                    { duration: options.showDuration, easing: options.showEasing, complete: options.onShown }
                );

                if (options.timeOut > 0) {
                    intervalId = setTimeout(hideToast, options.timeOut);
                }

                $toastElement.hover(stickAround, delayedHideToast);
                if (!options.onclick && options.tapToDismiss) {
                    $toastElement.click(hideToast);
                }

                if ($closeElement) {
                    $closeElement.click(function (event) {
                        if( event.stopPropagation ) {
                            event.stopPropagation();
                        } else if( event.cancelBubble !== undefined && event.cancelBubble !== true ) {
                            event.cancelBubble = true;
                        }

                        if (options.onCloseClick) {
                            options.onCloseClick(event);
                        }

                        hideToast(true);
                    });
                }

                if (options.onclick) {
                    $toastElement.click(function () {
                        options.onclick();
                        hideToast();
                    });
                }

                publish(response);

                if (options.debug && console) {
                    console.log(response);
                }

                return $toastElement;

                function hideToast(override) {
                    if ($(':focus', $toastElement).length && !override) {
                        return;
                    }
                    return $toastElement[options.hideMethod]({
                        duration: options.hideDuration,
                        easing: options.hideEasing,
                        complete: function () {
                            removeToast($toastElement);
                            if (options.onHidden && response.state !== 'hidden') {
                                options.onHidden();
                            }
                            response.state = 'hidden';
                            response.endTime = new Date();
                            publish(response);
                        }
                    });
                }

                function delayedHideToast() {
                    if (options.timeOut > 0 || options.extendedTimeOut > 0) {
                        intervalId = setTimeout(hideToast, options.extendedTimeOut);
                    }
                }

                function stickAround() {
                    clearTimeout(intervalId);
                    $toastElement.stop(true, true)[options.showMethod](
                        { duration: options.showDuration, easing: options.showEasing }
                    );
                }
            }

            function getOptions() {
                return $.extend({}, getDefaults(), toastr.options);
            }

            function removeToast($toastElement) {
                if (!$container) { $container = getContainer(); }
                if ($toastElement.is(':visible')) {
                    return;
                }
                $toastElement.remove();
                $toastElement = null;
                if ($container.children().length === 0) {
                    $container.remove();
                }
            }
            //#endregion

        })();
    });
}(typeof define === 'function' && define.amd ? define : function (deps, factory) {
    if (typeof module !== 'undefined' && module.exports) { //Node
        module.exports = factory(require('jquery', 'lodash'));
    } else {
        window['toastr'] = factory(window['jQuery'], window['_']);
    }
}));
