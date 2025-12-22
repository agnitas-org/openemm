(() => {

  class Colorpicker {

    static DATA_KEY = 'agn:colorpicker';

    static get($el) {
      return $el.data(Colorpicker.DATA_KEY);
    }

    constructor($el) {
      this.$el = $el;
      this.$trigger = this.#createTriggerInput();

      $el.spectrum({
        showInput: true,
        allowEmpty: true,
        preferredFormat: "hex",
        move: color => {
          $el.val(color.toHexString());
          this.$trigger.val(color.toHexString());
          this.#updatePipetteColor(color);
        },
        change: color => {
          this.#updatePipetteColor(color);
        }
      });

      this.#updatePipetteColor($el.spectrum('get'));
      $el.data(Colorpicker.DATA_KEY, this);
    }

    #createTriggerInput() {
      const $input = this.$el.clone();

      if (this.$el.attr('id')) {
        $input.attr('id', this.$el.attr('id') + '_colorpicker');
      }

      $input.attr('name', '');
      $input.removeClass('js-colorpicker');
      this.$el.before($input);

      $input.on('input', () => {
        const color = $input.val();
        this.$el.spectrum('set', color);
        this.#updatePipetteColor(this.$el.spectrum('get'));
      });

      $input.on('blur', () => $input.val(this.$el.val()));
      this.$el.on('change', () => {
        $input.val(this.$el.val());
        this.$el.spectrum('set', this.$el.val());
      });

      return $input;
    }

    #updatePipetteColor(selectedColor) {
      const color = selectedColor
        ? this.#invertColor(selectedColor.toHexString())
        : this.$el.css('--bs-body-color');

      this.$el.parent().find('.sp-preview').css('color', color);
    }

    #invertColor(hex) {
      return AGN.Lib.Helpers.getColorLuminance(hex) > 0.5 ? '#000' : '#FFF';
    }
  }

  AGN.Lib.Colorpicker = Colorpicker;

})();
