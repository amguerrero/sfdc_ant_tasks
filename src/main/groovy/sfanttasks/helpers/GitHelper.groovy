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

	def getFilesModifiedBetweenCommits(def commit1, def commit2) {
		def diffTree = "git --git-dir=${gitBaseDir}/.git diff-tree --no-commit-id --name-only -r $commit1 $commit2".execute()
		diffTree.waitFor()

		if (diffTree.exitValue()) {
			throw new RuntimeException(diffTree.err.text)
		}

		diffTree
	}

	def getFilesModifiedSince(def commit) {
		getFilesModifiedBetweenCommits("HEAD", commit)
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