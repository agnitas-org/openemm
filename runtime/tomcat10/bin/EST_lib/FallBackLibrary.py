class Version:
	version: tuple

	def __init__(self, version):
		self.release = self._parseVersionString(version)
		self.major, self.minor, self.micro, self.hotfix = (self.release + (0, 0, 0, 0))[:4]

	@staticmethod
	def _parseVersionString(versionString):
		return tuple(map(int, versionString.lower().lstrip("v").split(".")))

	def __eq__(self, otherVersion):
		if isinstance(otherVersion, Version):
			return self.release == otherVersion.version
		elif isinstance(otherVersion, tuple) and all(isinstance(item, int) for item in otherVersion):
			return self.release == otherVersion
		elif isinstance(otherVersion, str):
			try:
				return self.release == self._parseVersionString(otherVersion)
			except (TypeError, ValueError):
				return NotImplemented
		return NotImplemented

	def __lt__(self, otherVersion):
		if isinstance(otherVersion, Version):
			return self.release < otherVersion.version
		elif isinstance(otherVersion, tuple) and all(isinstance(item, int) for item in otherVersion):
			return self.release < otherVersion
		elif isinstance(otherVersion, str):
			try:
				return self.release < self._parseVersionString(otherVersion)
			except (TypeError, ValueError):
				return NotImplemented
		return NotImplemented

	def __le__(self, otherVersion):
		if isinstance(otherVersion, Version):
			return self.release <= otherVersion.version
		elif isinstance(otherVersion, tuple) and all(isinstance(item, int) for item in otherVersion):
			return self.release <= otherVersion
		elif isinstance(otherVersion, str):
			try:
				return self.release <= self._parseVersionString(otherVersion)
			except (TypeError, ValueError):
				return NotImplemented
		return NotImplemented

	def __gt__(self, otherVersion):
		if isinstance(otherVersion, Version):
			return self.release > otherVersion.version
		elif isinstance(otherVersion, tuple) and all(isinstance(item, int) for item in otherVersion):
			return self.release > otherVersion
		elif isinstance(otherVersion, str):
			try:
				return self.release > self._parseVersionString(otherVersion)
			except (TypeError, ValueError):
				return NotImplemented
		return NotImplemented

	def __ge__(self, otherVersion):
		if isinstance(otherVersion, Version):
			return self.release >= otherVersion.version
		elif isinstance(otherVersion, tuple) and all(isinstance(item, int) for item in otherVersion):
			return self.release >= otherVersion
		elif isinstance(otherVersion, str):
			try:
				return self.release >= self._parseVersionString(otherVersion)
			except (TypeError, ValueError):
				return NotImplemented
		return NotImplemented

	def __ne__(self, otherVersion):
		if isinstance(otherVersion, Version):
			return self.release != otherVersion.version
		elif isinstance(otherVersion, tuple) and all(isinstance(item, int) for item in otherVersion):
			return self.release != otherVersion
		elif isinstance(otherVersion, str):
			try:
				return self.release != self._parseVersionString(otherVersion)
			except (TypeError, ValueError):
				return NotImplemented
		return NotImplemented
