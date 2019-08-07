module.exports = {
  main: {
    expand                  : true,
    cwd                     : 'assets/core/images/sprite',
    src                     : ['*.svg'],
    dest                    : 'assets',
    options                 : {
      // shape               : {
      //   dimension       : {         // Set maximum dimensions
      //     maxWidth    : 32,
      //     maxHeight   : 32
      //   },
      //   spacing         : {         // Add padding
      //     padding     : 0
      //   }
      // },
      mode                : {
        // SVG Sprite Icons
        // view            : {         // Activate the «view» mode
        //   dest        : './',
        //   prefix      : '.icon-svg',
        //   bust        : false,
        //   sprite      : 'icon-sprite.svg',
        //   render      : {
        //     scss    : {
        //       dest: 'sass/modules/svg-icons.scss'
        //     }
        //   }
        // },
        defs          : {
          dest        : './',
          sprite      : 'icons-defs.svg'
        }
      }
    }
  },
}
