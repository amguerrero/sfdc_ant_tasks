package sfanttasks.sfnodesubstitution

import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class SFNodeSubstitutionTaskTest {
    private final static TEST_RESOURCES = 'src/test/resources/sfnodesubstitution'
    private SFNodeSubstitutionTask victim

    @Before
    void setup() {
        victim = new SFNodeSubstitutionTask()
    }

    @Test
    void "Always Substitute - Single Substitution"() {
        // Test Setup
        victim.config = [
                object: [ always: [ 'fields.fullName': 'New Name'] ],
                profile: [ always: [ 'classAccesses.enabled': 'New Enabled Value'] ]
        ]
        victim.srcFolder = "${TEST_RESOURCES}/Always_Substitute_Single_Substitution"

        Path originalTestObject = Paths.get("${TEST_RESOURCES}/original/objects/Test_Object__c.object")
        def resultTestObject = Paths.get("${victim.srcFolder}/src/objects/Test_Object__c.object")
        Path originalTestProfile = Paths.get("${TEST_RESOURCES}/original/profiles/Test_Profile__c.profile")
        def resultTestProfile = Paths.get("${victim.srcFolder}/src/profiles/Test_Profile__c.profile")
        Files.copy(originalTestObject, resultTestObject, StandardCopyOption.REPLACE_EXISTING)
        Files.copy(originalTestProfile, resultTestProfile, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestObject = Paths.get("${TEST_RESOURCES}/expected/Always_Substitute_Single_Substitution/objects/Test_Object__c.object")
        Path expectedTestProfile = Paths.get("${TEST_RESOURCES}/expected/Always_Substitute_Single_Substitution/profiles/Test_Profile__c.profile")
        Assert.assertEquals(expectedTestObject.getText('UTF-8'), resultTestObject.getText('UTF-8'))
        Assert.assertEquals(expectedTestProfile.getText('UTF-8'), resultTestProfile.getText('UTF-8'))
    }

    @Test
    void "Always Substitute - Multiple Substitution"() {
        // Test Setup
        victim.config = [
                profile: [ always: [ 'classAccesses.enabled': 'New Enabled Value', 'custom': 'Say What?!'] ]
        ]
        victim.srcFolder = "${TEST_RESOURCES}/Always_Substitute_Multiple_Substitution"

        Path originalTestProfile = Paths.get("${TEST_RESOURCES}/original/profiles/Test_Profile__c.profile")
        def resultTestProfile = Paths.get("${victim.srcFolder}/src/profiles/Test_Profile__c.profile")
        Files.copy(originalTestProfile, resultTestProfile, StandardCopyOption.REPLACE_EXISTING)

        // Run test
        victim.execute()

        // Check test
        Path expectedTestProfile = Paths.get("${TEST_RESOURCES}/expected/Always_Substitute_Multiple_Substitution/profiles/Test_Profile__c.profile")
        Assert.assertEquals(expectedTestProfile.getText('UTF-8'), resultTestProfile.getText('UTF-8'))
    }
}