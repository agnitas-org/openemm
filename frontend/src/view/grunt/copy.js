module.exports = {
  main: {
    files: [
      {
        expand: true,
        cwd: '<%= config.assets %>/',
        src: [
          '**',
          '!sass/**',
          '!js/**'
        ],
        dest: '<%= config.dist %>/'
      }
    ]
  },
  assets_for_styleguide: {
    files: [
      {
        expand: true,
        cwd: '<%= config.view %>/',
        src: [
          'favicon.ico'
        ],
        dest: '<%= config.docs %>/src/assets'
      },
      {
        expand: true,
        cwd: '<%= config.assets %>/',
        src: [
          'fonts/**',
          'core/**',
          'icons-defs.svg',
          'application.css',
          'application.redesigned.css',
          'application.redesigned.js',
          'application.js'
        ],
        dest: '<%= config.docs %>/src/assets'
      }
    ]
  }
}
