package sfanttasks.sfmetadatacleanup

class MatchOperations {
    static Boolean matches(String matchString, String value) {
        value =~ /$matchString/
    }

    static Boolean doesNotMatch(String matchString, String value) {
        !matches(matchString, value)
    }

    static Boolean startsWith(String matchString, String value) {
        value.startsWith(matchString)
    }

    static Boolean doesNotStartWith(String matchString, String value) {
        !value.startsWith(matchString)
    }

    static Boolean endsWith(String matchString, String value) {
        value.endsWith(matchString)
    }

    static Boolean doesNotEndWith(String matchString, String value) {
        !value.endsWith(matchString)
    }

    static Boolean contains(String matchString, String value) {
        value.contains(matchString)
    }

    static Boolean doesNotContain(String matchString, String value) {
        !value.contains(matchString)
    }

    static Boolean equalTo(String matchString, String value) {
        value == matchString
    }

    static Boolean notEqualTo(String matchString, String value) {
        value != matchString
    }

    static Boolean isManagedPackage(String isManagePackageString, String value) {
        String matchManagedPackageItems = '^([^\\.]+__[^\\.]+__[^\\.]{1,3}|[^\\.]+__[^\\.]+__[^\\.]{1,3}\\.[^\\.]+|.+\\.[^\\.]+__[^\\.]+__[^\\.]{1,3})$'

        Boolean response = matches(matchManagedPackageItems, value)

        (isManagePackageString.toBoolean()) ? response : !response
    }

    static Boolean always(String matchString, String value) {
        true
    }
}
