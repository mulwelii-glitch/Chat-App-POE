package chatapp;

import org.junit.Test;
import static org.junit.Assert.*;


public class LoginTest {

    @Test
    public void testUsernameCorrectlyFormatted() {
        Login loginObj = new Login();
        boolean result = loginObj.checkUserName("kyl_1");
        assertTrue("Username verification failed on valid input.", result);
    }

    @Test
    public void testUsernameIncorrectlyFormatted() {
        Login loginObj = new Login();
        boolean result = loginObj.checkUserName("kyle!!!");
        assertFalse("Username verification failed to catch invalid formatting.", result);
    }

    @Test
    public void testPasswordMeetsComplexity() {
        Login loginObj = new Login();
        boolean result = loginObj.checkPasswordComplexity("Ch&&sec@ke99!");
        assertTrue("Password complexity check failed on a valid password setup.", result);
    }

    @Test
    public void testPasswordDoesNotMeetComplexity() {
        Login loginObj = new Login();
        boolean result = loginObj.checkPasswordComplexity("password");
        assertFalse("Password complexity matrix failed to catch weak credentials.", result);
    }

    @Test
    public void testCellPhoneNumberCorrect() {
        Login loginObj = new Login();
        boolean result = loginObj.checkCellPhoneNumber("+27838968976");
        assertTrue("Cell check rejected a properly formatted South African mobile line.", result);
    }

    @Test
    public void testCellPhoneNumberIncorrect() {
        Login loginObj = new Login();
        boolean result = loginObj.checkCellPhoneNumber("08966553");
        assertFalse("Cell check allowed an incorrectly formatted phone number configuration.", result);
    }
}