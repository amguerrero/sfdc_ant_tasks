package sfanttasks.helpers

class GitHelper {
	private def gitBaseDir = "."

	private GitHelper() {
	}

	static def withGitBaseDir(def gitBaseDir) {
		def instance = new GitHelper()
		instance.gitBaseDir = gitBaseDir

		instance
	}

	def getFilesModifiedBetweenCommits(def olderCommit, def newerCommit) {
		def diffTree = "git --git-dir=${gitBaseDir}/.git diff-tree --no-commit-id --name-only -r $olderCommit $newerCommit".execute()
		diffTree.waitFor()

		if (diffTree.exitValue()) {
			throw new RuntimeException(diffTree.err.text)
		}

		diffTree
	}

	def getFilesModifiedSince(def commit) {
		getFilesModifiedBetweenCommits(commit, "HEAD")
	}

	def getPreviousVersionOf(def file, def commit) {
		def gitFile = getGitFileName(file)
		def previousVersion = "git --git-dir=${gitBaseDir}/.git cat-file -p $commit:\"$gitFile\"".execute()

		previousVersion
	}

	private def getGitFileName(def path) {
		def gitNames = []
		for (int i=0; i < path.getNameCount(); i++) {
			gitNames << path.getName(i).toString()
		}

		gitNames.join("/")
	}
}