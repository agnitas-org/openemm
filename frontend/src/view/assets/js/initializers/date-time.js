// /*doc
// ---
// title: Datepicker Directive
// name: datepicker-directive
// parent: directives
// ---
//
// An input can be decorated with a datepicker using the `js-datepicker` class. Options for the datepicker can be passed via the `data-datepicker-options` attribute. For aligning the datepicker on the right side you can pass the `js-datepicker-right` class.
//
// A documentation of the available options can be found under <a href="http://amsul.ca/pickadate.js/date/" target="_blank">PickADate Doc</a>
//
// ```htmlexample
// <div class="form-group">
//     <div class="col-sm-4">
//         <label class="control-label">Date</label>
//     </div>
//     <div class="col-sm-8">
//         <div class="input-group">
//             <div class="input-group-controls">
//                 <input type="text" class="form-control datepicker-input js-datepicker" value="01.12.2015" />
//             </div>
//             <div class="input-group-btn">
//                 <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
//                     <i class="icon icon-calendar-o"></i>
//                 </button>
//             </div>
//         </div>
//     </div>
// </div>
// ```
// */
//
// /*doc
// ---
// title: Timepicker Directive
// name: timepicker-directive
// parent: directives
// ---
//
// An input can be decorated with an input-mask for time by using the `js-timepicker` class. Options for the input-mask can be passed via the `data-timepicker-options` attribute. This is mainly used to define the mask:
//
// data-timepicker-options | Used for
// ----------------|-----------------
// `mask: 'h:halfs'` | Default, full control over hours, with 30 minute increments
// `mask: 'h:s'` | full control
// `mask: 'h:00'` | full control over hours, minutes stay at 00
// `mask: 'h:quarts'` | full control over hours, with 15 minute increments
//
//
// A documentation of the underlying jquery plugin can be found under <a href="https://github.com/RobinHerbots/jquery.inputmask" target="_blank">Jquery Inputmask Doc</a>
//
// <strong>CAVEAT: When passing a value to the input make sure it is zero-padded (08:45 instead of 8:45)</strong>
//
// ```htmlexample
// <div class="form-group">
//     <div class="col-sm-4">
//         <label class="control-label">Time (Default)</label>
//     </div>
//     <div class="col-sm-8">
//         <div class="input-group">
//             <div class="input-group-controls">
//                 <input type="text" value="08:30" class="form-control js-timepicker" />
//             </div>
//             <div class="input-group-addon">
//                 <span class="addon">
//                     <i class="icon icon-clock-o"></i>
//                 </span>
//             </div>
//         </div>
//     </div>
// </div>
// <div class="form-group">
//     <div class="col-sm-4">
//         <label class="control-label">Time h:s</label>
//     </div>
//     <div class="col-sm-8">
//         <div class="input-group">
//             <div class="input-group-controls">
//                 <input type="text" value="08:22" class="form-control js-timepicker" data-timepicker-options="mask: 'h:s'" />
//             </div>
//             <div class="input-group-addon">
//                 <span class="addon">
//                     <i class="icon icon-clock-o"></i>
//                 </span>
//             </div>
//         </div>
//     </div>
// </div>
// <div class="form-group">
//     <div class="col-sm-4">
//         <label class="control-label">Time h:00</label>
//     </div>
//     <div class="col-sm-8">
//         <div class="input-group">
//             <div class="input-group-controls">
//                 <input type="text" value="08:00" class="form-control js-timepicker" data-timepicker-options="mask: 'h:00'" />
//             </div>
//             <div class="input-group-addon">
//                 <span class="addon">
//                     <i class="icon icon-clock-o"></i>
//                 </span>
//             </div>
//         </div>
//     </div>
// </div>
// <div class="form-group">
//     <div class="col-sm-4">
//         <label class="control-label">Time h:quarts</label>
//     </div>
//     <div class="col-sm-8">
//         <div class="input-group">
//             <div class="input-group-controls">
//                 <input type="text" value="08:15" class="form-control js-timepicker" data-timepicker-options="mask: 'h:quarts'" />
//             </div>
//             <div class="input-group-addon">
//                 <span class="addon">
//                     <i class="icon icon-clock-o"></i>
//                 </span>
//             </div>
//         </div>
//     </div>
// </div>
// ```
// */

;(function(){

    var Helpers = AGN.Lib.Helpers;

    AGN.Lib.CoreInitializer.new('pickadate', function ($scope) {
        if (!$scope) {
            $scope = $(document);
        }

        _.each($scope.find('.js-datepicker'), function (input) {
            var $input = $(input),
                $picker,
                options,
                baseOptions = {
                    editable: true,
                    min: new Date(0, 0, 0),
                    max: new Date(9999, 11, 31),
                    maxFor: undefined,
                    minFor: undefined,
                    klass: {
                        holder: 'datepicker__holder picker__holder' + ( $input.hasClass('js-datepicker-right') ? ' datepicker__holder-right' : '' )
                    }
                };

            options = _.merge({}, baseOptions, Helpers.objFromString($input.data('datepicker-options')));

            $picker = $input.pickadate('picker');
            if ($picker && $picker.get('start')) {
                // prevent double initializing
                return;
            }
            $(input).pickadate(options);
            $picker = $input.pickadate('picker');

            $picker.on('open', function () {
                this.$node.parents('.input-group').find('.js-open-datepicker').addClass('is-active');
            });

            $picker.on('close', function () {
                this.$node.parents('.input-group').find('.js-open-datepicker').removeClass('is-active');
            });

            if (options.maxFor) {
                $input.on('change', function () {
                    var maximum = $(this).val() || options.min;
                    if (maximum) {
                        $(options.maxFor).pickadate('picker').set('max', maximum);
                    }
                })
            }

            if (options.minFor) {
                $input.on('change', function () {
                    var minimum = $(this).val() || options.max;
                    if (minimum) {
                        $(options.minFor).pickadate('picker').set('min', minimum);
                    }
                })
            }

            var changeHandlerEnabled = true;

            $input.on('change', function() {
                if (changeHandlerEnabled) {
                    var $this = $(this);
                    var picker = $this.pickadate('picker');

                    try {
                        changeHandlerEnabled = false;
                        if ($this.val()) {
                            picker.set('select', $this.val());
                        } else {
                            picker.set('clear');
                        }
                    } finally {
                        changeHandlerEnabled = true;
                    }
                }
            });
        });

        _.each($scope.find('.js-timepicker'), function (input) {
            var $input = $(input),
                options,
                mask,
                baseOptions = {
                    mask: "h:halfs",
                    oncomplete: function () {
                        $input.trigger('timepicker:complete')
                    },
                    onincomplete: function () {
                        $input.trigger('timepicker:incomplete')
                    }
                };

            options = _.merge({}, baseOptions, Helpers.objFromString($input.data('timepicker-options')));

            mask = options.mask;
            delete options.mask;

            $input.inputmask(mask, options);
            $input.attr('placeholder', $input.data('_inputmask').opts.placeholder)

        });

    });

})();
