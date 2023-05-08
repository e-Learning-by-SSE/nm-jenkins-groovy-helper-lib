def call(String npmrcId, String npmArgs = '') {
    def versionList
    withNPM(npmrcConfig: npmrcId) {
        if (!isAlreadyPublished()) {
            sh "npm publish ${npmArgs}"
        }
    }
}

// package.json must be in workdir for this method
def isAlreadyPublished() {
    return hasRemoteNpmVersion(pkgName: packageJson.getPkgName(), targetVersion: packageJson.getVersion())
}

def hasRemoteNpmVersion(Map config = [:]) {
    def versionList = packageJson.scriptOut("npm view ${config.pkgName} versions --json 2>&1 && echo || echo")
    def versionList = sh(returnStdout: true, script: "")
    def json = readJSON(text: versionList)

    if (json) {
      def versions = json.collect { it.toString() }
      if (versions.contains(config.targetVersion)) {
        return true
    }
    return false
}