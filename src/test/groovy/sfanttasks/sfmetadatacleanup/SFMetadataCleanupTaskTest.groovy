package sfanttasks.sfmetadatacleanup

import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class SFMetadataCleanupTaskTest {
    private final static TEST_RESOURCES = 'src/test/resources/sfmetadatacleanup'
    private SFMetadataCleanupTask victim

    @Before
    void setup() {
        victim = new SFMetadataCleanupTask()
    }
	
	@Test
	void “Must Fail”() {
		Assert.fail(“Just Fail”)
	}

    @Test
    void "Default Config - From hardcoded config"() {
        // Test setup
        def currentTestSubdir = 'DefaultConfig_Objects_RemovesListViews'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Path originalTestProfile = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/profiles/Test_Profile__c.profile")
        def resultTestProfile = Paths.get("${victim.srcFolder}/src/profiles/Test_Profile__c.profile")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)
        Files.copy(originalTestProfile, resultTestProfile, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Test_Object__c.object")
        Path expectedTestProfile = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/profiles/Test_Profile__c.profile")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
        Assert.assertEquals(resultTestProfile.getText('UTF-8'), expectedTestProfile.getText('UTF-8'))
    }

    @Test
    void "Default Config - From Config File metadataCleanupConfig_removeNegatives.json"() {
        // Test setup
        def currentTestSubdir = 'DefaultConfig_Objects_RemovesListViews'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "config/metadataCleanupConfig_removeNegatives.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Path originalTestProfile = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/profiles/Test_Profile__c.profile")
        def resultTestProfile = Paths.get("${victim.srcFolder}/src/profiles/Test_Profile__c.profile")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)
        Files.copy(originalTestProfile, resultTestProfile, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Test_Object__c.object")
        Path expectedTestProfile = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/profiles/Test_Profile__c.profile")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
        Assert.assertEquals(resultTestProfile.getText('UTF-8'), expectedTestProfile.getText('UTF-8'))
    }

    @Test
    void "Clean Using Matches - Custom config"() {
        // Test setup
        def currentTestSubdir = 'Clean_Using_Matches'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "${TEST_RESOURCES}/${currentTestSubdir}/matchesConfig.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Test_Object__c.object")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
    }

    @Test
    void "Clean Using Does Not Match - Custom config"() {
        // Test setup
        def currentTestSubdir = 'Clean_Using_Matches'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "${TEST_RESOURCES}/${currentTestSubdir}/doesNotMatchConfig.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Test_Object__c.object")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
    }

    @Test
    void "Clean Using Starts With - Custom config"() {
        // Test setup
        def currentTestSubdir = 'Clean_Using_StartsWith'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "${TEST_RESOURCES}/${currentTestSubdir}/startsWithConfig.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Test_Object__c.object")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
    }

    @Test
    void "Clean Using Does NOT Start With - Custom config"() {
        // Test setup
        def currentTestSubdir = 'Clean_Using_StartsWith'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "${TEST_RESOURCES}/${currentTestSubdir}/doesNotStartWithConfig.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Test_Object__c.object")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
    }

    @Test
    void "Clean Using Ends With - Custom config"() {
        // Test setup
        def currentTestSubdir = 'Clean_Using_EndsWith'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "${TEST_RESOURCES}/${currentTestSubdir}/endsWithConfig.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Test_Object__c.object")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
    }

    @Test
    void "Clean Using Does NOT End With - Custom config"() {
        // Test setup
        def currentTestSubdir = 'Clean_Using_EndsWith'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "${TEST_RESOURCES}/${currentTestSubdir}/doesNotEndWithConfig.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Test_Object__c.object")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
    }

    @Test
    void "Clean Using Contains - Custom config"() {
        // Test setup
        def currentTestSubdir = 'Clean_Using_Contains'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "${TEST_RESOURCES}/${currentTestSubdir}/containsConfig.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Test_Object__c.object")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
    }

    @Test
    void "Clean Using Does NOT Contain - Custom config"() {
        // Test setup
        def currentTestSubdir = 'Clean_Using_Contains'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "${TEST_RESOURCES}/${currentTestSubdir}/doesNotContainConfig.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Test_Object__c.object")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
    }

    @Test
    void "Clean Using Equal To - Custom config"() {
        // Test setup
        def currentTestSubdir = 'Clean_Using_EqualTo'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "${TEST_RESOURCES}/${currentTestSubdir}/equalToConfig.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Test_Object__c.object")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
    }

    @Test
    void "Clean Using NOT Equal To - Custom config"() {
        // Test setup
        def currentTestSubdir = 'Clean_Using_EqualTo'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "${TEST_RESOURCES}/${currentTestSubdir}/notEqualToConfig.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Test_Object__c.object")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
    }

    @Test
    void "Clean Using Is Managed Package - Custom config"() {
        // Test setup
        def currentTestSubdir = 'Clean_Using_IsManagedPackage'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "${TEST_RESOURCES}/${currentTestSubdir}/isManagedPackageConfig.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Is_Managed_Package_Test_Object__c.object")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
    }

    @Test
    void "Clean Using is NOT Managed Package - Custom config"() {
        // Test setup
        def currentTestSubdir = 'Clean_Using_IsManagedPackage'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "${TEST_RESOURCES}/${currentTestSubdir}/isNotManagedPackageConfig.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Is_NOT_Managed_Package_Test_Object__c.object")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
    }

    @Test
    void "Clean Using Always - Custom config"() {
        // Test setup
        def currentTestSubdir = 'Clean_Using_Always'
        victim.srcFolder = "${TEST_RESOURCES}/${currentTestSubdir}"
        victim.configFile = "${TEST_RESOURCES}/${currentTestSubdir}/alwaysConfig.json"
        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/${currentTestSubdir}/src/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/${currentTestSubdir}/src/objects/Test_Object__c.object")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
    }
}
