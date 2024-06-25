module.exports = {
  build: {
    files: {
      '<%= config.assets %>/application.css': '<%= config.assets %>/sass/application.scss',
      '<%= config.assets %>/help.css': '<%= config.assets %>/sass/help.scss'
    },
    options: {
      implementation: require('sass'),
      style: 'nested',
      sourceMap: true
    }
  },
  build_redesigned: {
    files: {
      '<%= config.assets %>/application.redesigned.css': '<%= config.assets %>/sass_redesign/application-static.scss',
    },
    options: {
      implementation: require('sass'),
      style: 'nested',
      sourceMap: true
    }
  },
  build_landing: {
    files: {
      '<%= config.assets %>/landing.css': '<%= config.assets %>/sass_redesign/landing.scss',
    },
    options: {
      implementation: require('sass'),
      style: 'nested',
      sourceMap: true
    }
  },
  build_formcss: {
    files: {
      '<%= config.assets %>/form.css': '<%= config.assets %>/sass/form.scss',
    },
    options: {
      implementation: require('sass'),
      style: 'nested',
      sourceMap: true
    }
  }
};
