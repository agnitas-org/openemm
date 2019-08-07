module.exports = {
  compile: {
    files: {
      '<%= config.assets %>/application.min.css': '<%= config.assets %>/application.css',
      '<%= config.assets %>/help.min.css': '<%= config.assets %>/help.css'
    },
    options: {
      keepSpecialComments: 0
    }
  }
}
