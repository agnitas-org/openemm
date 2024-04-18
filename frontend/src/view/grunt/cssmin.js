module.exports = {
  compile: {
    files: {
      '<%= config.assets %>/application.min.css': '<%= config.assets %>/application.css',
      '<%= config.assets %>/help.min.css': '<%= config.assets %>/help.css'
    },
    options: {
      keepSpecialComments: 0
    }
  },
  compile_redesigned: {
    files: {
      '<%= config.assets %>/application.redesigned.min.css': '<%= config.assets %>/application.redesigned.css',
      '<%= config.assets %>/help.min.css': '<%= config.assets %>/help.css'
    },
    options: {
      keepSpecialComments: 0
    }
  },
  compile_landing: {
    files: {
      '<%= config.assets %>/landing.min.css': '<%= config.assets %>/landing.css',
    },
    options: {
      keepSpecialComments: 0
    }
  }
}
