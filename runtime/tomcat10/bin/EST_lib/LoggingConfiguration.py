import logging.handlers
from pathlib import Path

import logging
import logging.config

def setup_logging(loggingDirectory: str | Path, logLevel=logging.ERROR):
	loggingDirectory = Path(loggingDirectory)
	loggingDirectory.mkdir(exist_ok=True,parents=True)

	logging.config.dictConfig(
	{
		"version": 1,
		"disable_existing_loggers": False,
		"formatters": {
			"standard": {
				"format": "%(asctime)s [%(levelname)s] %(name)s: %(message)s"
			},
		},
		"handlers": {
			"general_file": {
				"class": "logging.handlers.RotatingFileHandler",
				"filename": loggingDirectory / "support_tool.log",
				"formatter": "standard",
				"maxBytes": 10485760,
				"backupCount": 5
			},
			"database_log_file": {
				"class": "logging.handlers.RotatingFileHandler",
				"filename": loggingDirectory / "db-operations.log",
				"formatter": "standard",
				"maxBytes": 10485760,
				"backupCount": 5
			},
			"update_log_file": {
				"class": "logging.handlers.RotatingFileHandler",
				"filename": loggingDirectory / "update.log",
				"formatter": "standard",
				"maxBytes": 10485760,
				"backupCount": 5
			},
		},
		"loggers": {
			"": {
				"handlers": ["general_file"],
				"level": logLevel,
			},
			"db-operations": {
				"handlers": ["database_log_file"],
				"level": logLevel,
				"propagate": True,
			},
			"update": {
				"handlers": ["update_log_file"],
				"level": logLevel,
				"propagate": True,
			},
		},
	})
