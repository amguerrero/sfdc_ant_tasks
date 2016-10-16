package sfanttasks.helpers

class GitHelper {
	static def getFilesModifiedSince(def commit) {
		def diffTree = "git diff-tree --no-commit-id --name-only -r HEAD $commit".execute()
		diffTree.waitFor()

		if (diffTree.exitValue()) {
			throw new RuntimeException(diffTree.err.text)
		}

		diffTree
	}

	static def getPreviousVersionOf(def file, def commit) {
		def gitFile = getGitFileName(file)
		def previousVersion = "git cat-file -p $commit:\"$gitFile\"".execute()

		previousVersion
	}

	private static def getGitFileName(def path) {
		def gitNames = []
		for (int i=0; i < path.getNameCount(); i++) {
			gitNames << path.getName(i).toString()
		}

		gitNames.join("/")
	}
}