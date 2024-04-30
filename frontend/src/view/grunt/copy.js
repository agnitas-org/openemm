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
        cwd: '<%= config.assets %>/',
        src: [
          'fonts/**',
          'icons-defs.svg',
          'application.css',
          'application.js',
          'translation.js',
        ],
        dest: '<%= config.docs %>/src/assets'
      }
    ]
  }
}
