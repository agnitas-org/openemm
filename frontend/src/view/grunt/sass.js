module.exports = {
  build: {
    files: {
      '<%= config.assets %>/application.css': '<%= config.assets %>/sass/application.scss',
      '<%= config.assets %>/help.css': '<%= config.assets %>/sass/help.scss'
    },
    options: {
      style: 'nested',
      sourceMap: true
    }
  }
};
