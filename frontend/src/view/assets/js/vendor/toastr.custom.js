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
            const CAROUSEL_TABS_THRESHOLD = 10;
            const POPUP_WIDTH = parseInt($('html').css('--popup-width'));
            const POPUP_WIDTH_MOBILE = parseInt($('html').css('--popup-width-mobile'));
            
            var $container;
            var listener;
            var toastId = 0;
            var toastType = {
                alert: 'alert',
                info: 'info',
                success: 'success',
                warning: 'warning'
            };
            const POPUPS_ORDER = [toastType.success, toastType.warning, toastType.alert, toastType.info];

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
                    removeToast($toastElement, options.removeEmptyContainer);
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
                        complete: function () { removeToast($toastElement, options.removeEmptyContainer); }
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
                    toastClass: 'popup',
                    containerId: 'popups',
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
                    positionClass: 'popups-bottom-right',
                    timeOut: 5000, // Set timeOut and extendedTimeout to 0 to make it sticky
                    titleClass: 'headline',
                    messageClass: 'popup-content',
                    tabsClass: 'popup-tabs',
                    tabClass: 'popup-tab',
                    arrowClass: 'arrow',
                    leftArrowClass: 'arrow-left',
                    rightArrowClass: 'arrow-right',
                    headerClass: 'popup-header',
                    activeTabClass: 'active',
                    hasTabsClass: 'has-tabs',
                    hasCarouselClass: 'has-carousel',
                    expandedClass: 'expanded',
                    closeIcon: 'icon icon-times-circle',
                    target: 'body',
                    newestOnTop: false,
                    useTabs: true,
                    removeEmptyContainer: true,
                    collapse: true
                };
            }

            function publish(args) {
                if (!listener) { return; }
                listener(args);
            }

            function getTemplate() {
              return this._template || (this._template = `
                <div class="{{= toastClass }} {{= toastClass }}-{{= type }}">
                  <div class="{{= headerClass }} {{= tabsClass }}"></div>
                  <div class="{{= tabClass }} {{= arrowClass }} {{= leftArrowClass }} {{= activeTabClass }}">
                    <i class="icon icon-{{= leftArrowClass }}"></i>
                  </div>
                  <div class="{{= tabClass }} {{= arrowClass }} {{= rightArrowClass }} {{= activeTabClass }}">
                    <i class="icon icon-{{= rightArrowClass }}"></i>
                  </div>
                  
                  <div class="{{= headerClass }}">
                    <div class="d-flex">
                     {{ if (collapse) { }}
                        <i class="icon icon-caret-left me-2"></i>
                     {{ } }}
                     <i class="icon icon-state-{{= type }} popup-header-icon me-1"></i>
                    </div>
                    <span class="popup-header-title">{{= title }}</span>
                    <i class="js-close close-icon {{= closeIcon }} ms-auto"></i>
                  </div>
                  <div class="{{= messageClass }}" style="display: none;">{{= message }}</div>
                </div>`
              );
            }

            function notify(map) {
              var globalOptions = getOptions(),
                  options = $.extend(globalOptions, map);

                if (typeof map.options !== 'undefined') {
                  options = $.extend(options, map.options);
                }

                $container = getContainer(options, true);

                if ($container.is('[data-popups-options]')) {
                  const popupsOptions = AGN.Lib.Helpers.objFromString($container.data('popups-options'));
                  options = _.extend(options, popupsOptions);
                }

                // Only add a new tab if popup of this type already presented
                const $popup = $(`.popup-${options.type}`);
                if (options.useTabs && $popup.is(":visible")) {
                  addNewTab($popup);
                  return $popup; 
                }
                
                toastId++;

                var template = _.template(getTemplate())(options),
                    $template = $(template);


                var intervalId = null,
                    $toastElement = $template,
                    $closeElement = $toastElement.find('.js-close'),
                    $rightArrow = $toastElement.find(`.${options.rightArrowClass}`),
                    $leftArrow = $toastElement.find(`.${options.leftArrowClass}`).hide(),
                    tabIndex = 0,
                    response = {
                        toastId: toastId,
                        state: 'visible',
                        startTime: new Date(),
                        options: options
                    };

                if (options.message) {
                  $toastElement.find('.' + options.messageClass).show();
                }

                $toastElement.data('type', options.type)
                // $toastElement.hide();
                appendPopupToContainer($toastElement);


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

                        if ($toastElement.hasClass(options.hasTabsClass)) {
                          closePopupTab();
                        } else {
                          hideToast(true);
                        }
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

                $leftArrow.click(() => moveCarouselRight());
                $rightArrow.click(() => moveCarouselLeft());

                if (options.collapse) {
                  // collapse/expand popup by click on mobile
                  $toastElement.find(`.${options.headerClass}:not(.${options.tabsClass})`).click(() => {
                    $toastElement.toggleClass(options.expandedClass, isCollapsed($toastElement));
                    controlTabsDisplay($toastElement);
                  });
                } else {
                  $toastElement.addClass(options.expandedClass);
                }

                addNewTab($toastElement); // popup itself it's a first tab that invisible until second tab added

                return $toastElement;

              function getPopupToInsertBefore() {
                const newPopupOrder = POPUPS_ORDER.indexOf(options.type);
                
                return $container.find(`.${options.toastClass}`).filter((index, popup) => {
                  const currentPopupOrder = POPUPS_ORDER.indexOf($(popup).data('type'));
                  return newPopupOrder < currentPopupOrder;
                }).first();
              }

              function appendPopupToContainer($popup) {
                  if (options.newestOnTop || !$container.find(`.${options.toastClass}`).length) {
                    $container.prepend($popup);
                    return;
                  }
                  const $insertBefore = getPopupToInsertBefore();
                  if ($insertBefore.exists()) {
                    $popup.insertBefore($insertBefore);
                    return;
                  }
                  $container.append($popup);
                }
              
                function moveCarousel($popup, shift) {
                  const $carousel = $popup.find(`.${options.tabsClass}`);
                  $carousel.css("transform", `translateX(${shift}px)`);
                  setTimeout(() => controlBordersBetweenTabs($popup), 300);       
                }
                
                function moveCarouselRight(preventShowRightArrow) {
                  tabIndex = tabIndex - 1;
                  const shift = tabIndex * -getTabWidth($toastElement);
                  
                  moveCarousel($toastElement, shift)
    
                  if (!preventShowRightArrow) {
                    $rightArrow.show();
                  }
                  if (tabIndex === 0) {  // edge reached
                    $leftArrow.hide();
                  }
                }
    
                function moveCarouselLeft() {
                  tabIndex = tabIndex + 1;
                  const shift = tabIndex * getTabWidth($toastElement);
                  
                  moveCarousel($toastElement, shift * -1)
                  
                  $leftArrow.show();
                  if ($toastElement.find(`.${options.tabsClass}`).width() - shift <= $toastElement.width()) { // edge reached
                    $rightArrow.hide();
                  }
                }
                
                function closePopupTab() {
                  const $activeTab = getMessageTabs($toastElement).filter(`.${options.activeTabClass}`);
                  const $nextTab = $activeTab.next();
                  const $newActiveTab = $nextTab.length ? $nextTab : $activeTab.prev();
                  $activeTab.remove();
                  if ($leftArrow.is(':visible')) {
                    moveCarouselRight(true);
                  }
                  chooseTab($toastElement, $newActiveTab);
                }
                
                function controlTabsDisplay($popup) {
                  let $tabs = getMessageTabs($popup);
                  $popup.toggleClass(options.hasTabsClass, $tabs.length > 1);
                  $popup.toggleClass(options.hasCarouselClass, $tabs.length > CAROUSEL_TABS_THRESHOLD);
                  
                  const width = getTabWidth($popup)
                  $popup.find(`.${options.tabClass}`).each((i, tab) => $(tab).css('width', `${width}px`))
                  controlBordersBetweenTabs($popup);
                }
      
                function createTab$() {
                  const $tab = $(`
                    <div class="${options.tabClass}">
                      <i class="icon icon-state-${options.type}"></i>
                    </div>`)
                  $tab.data('options', options);
                  return $tab;
                }
    
                function chooseTab($popup, $tab) {
                  getMessageTabs($popup).filter(`.${options.activeTabClass}`).removeClass(options.activeTabClass);
                  $tab.addClass(options.activeTabClass);
                  $popup.find(`.${options.messageClass}`).html($tab.data('options').message);
                  controlTabsDisplay($popup);
                }
      
                function addNewTab($popup) {
                  const $tab = createTab$();
                  $popup.find(`.${options.tabsClass}`).prepend($tab);
                  chooseTab($popup, $tab);
                  $tab.click(() => chooseTab($popup, $tab));
                }
                
                function hideRightBorderOfTab($tab) {
                  $tab.style('border-right-width', '0');
                }
                
                function showRightBorderOfTab($tab) {
                  $tab.style('border-right-width', '1px');
                }
      
                function getTabsWithinPopup($popup, $tabs) {
                  const popupStart = $popup.offset().left;
                  const popupEnd = $popup.offset().left + $popup.outerWidth();
      
                  return $tabs.filter((i, tab) => {
                    const $tab = $(tab);
                    const tabStart = $tab.offset().left;
                    const tabEnd = $tab.offset().left + $tab.outerWidth();
                    return tabStart >= popupStart - 5 && tabEnd <= popupEnd + 5;
                  });
                }
      
                function controlBordersBetweenTabs($popup) {
                  let $tabs = getMessageTabs($popup);
                  $tabs.each((i, tab) => showRightBorderOfTab($(tab)));
                  hideRightBorderOfTab($tabs.filter(`.${options.activeTabClass}`).prev());
                  
                  const $tabsWithinPopup = getTabsWithinPopup($popup, $tabs);
                  if ($popup.find(`.${options.rightArrowClass}`).is(':visible')) {
                    hideRightBorderOfTab($tabsWithinPopup.last().prev());
                  }
                  if ($popup.find(`.${options.leftArrowClass}`).is(':visible')) {
                    hideRightBorderOfTab($tabsWithinPopup.first());
                  }
                }
      
                function getMessageTabs($popup) {
                  return $popup.find(`.${options.tabClass}:not(.${options.arrowClass})`);
                }
                
                function getTabWidth($popup) {
                  return !$popup.hasClass(options.expandedClass) || isCollapsed($popup)
                    ? POPUP_WIDTH / CAROUSEL_TABS_THRESHOLD
                    : $popup.width() / CAROUSEL_TABS_THRESHOLD;
                }
                
                function isCollapsed($popup) {
                  return $popup.width() === POPUP_WIDTH_MOBILE;
                }
                
                function hideToast(override) {
                    if (($(':focus', $toastElement).length || $toastElement.hasClass(options.expandedClass)) && !override) {
                        return;
                    }
                    return $toastElement[options.hideMethod]({
                        duration: options.hideDuration,
                        easing: options.hideEasing,
                        complete: function () {
                            removeToast($toastElement, options.removeEmptyContainer);
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

            function removeToast($toastElement, removeEmptyContainer = true) {
                if (!$container) { $container = getContainer(); }
                if ($toastElement.is(':visible')) {
                    return;
                }
                $toastElement.remove();
                $toastElement = null;
                if (removeEmptyContainer && $container.children().length === 0) {
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
