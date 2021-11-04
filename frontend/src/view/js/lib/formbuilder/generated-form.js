$(document).ready(function(){
    $('.js-datepicker').each(function () {
        var $input = $(this),
            $picker,
            options,
            baseOptions = {
                editable: true,
                min: new Date(0, 0, 0),
                max: new Date(9999, 11, 31),
                maxFor: undefined,
                minFor: undefined,
                klass: {
                    picker: 'picker form-picker',
                    holder: 'datepicker__holder picker__holder' + ( $input.hasClass('js-datepicker-right') ? ' datepicker__holder-right' : '' )
                }
            };

        options = $.extend(baseOptions, {format: 'yyyy/mm/dd', formatSubmit: 'yyyy/mm/dd'});//, Helpers.objFromString($input.data('datepicker-options')));

        $picker = $input.pickadate('picker');
        if ($picker && $picker.get('start')) {
            // prevent double initializing
            return;
        }
        $input.pickadate(options);
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

    $(document).on('click', '.js-open-datepicker', function(e) {
        var $this   = $(this),
            $input  = $this.parents('.input-group').find('.js-datepicker'),
            $picker = $input.pickadate('picker');
        $picker.open();
    });

    $(document).on('click', '.js-datepicker', function(e) {
        var $input   = $(this),
            $picker = $input.pickadate('picker');
        $picker.open();
        $input.focus();
    });
});
