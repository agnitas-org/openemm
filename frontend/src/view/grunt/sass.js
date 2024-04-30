module.exports = {
  build: {
    files: {
      '<%= config.assets %>/application.css': '<%= config.assets %>/sass/application.scss',
      '<%= config.assets %>/help.css': '<%= config.assets %>/sass/help.scss'
    },
    options: {
      implementation: require('node-sass'),
      style: 'nested',
      sourceMap: true
    }
  },
  build_formcss: {
    files: {
      '<%= config.assets %>/form.css': '<%= config.assets %>/sass/form.scss',
    },
    options: {
      implementation: require('node-sass'),
      style: 'nested',
      sourceMap: true
    }
  }
};
