package chatapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Part 3 unit tests. Every test starts from the five test-data messages from the task
 * (flag 1 = Sent, 2 = Disregard, 3 = Stored).
 */
public class MessageManagerTest {

    private static final String MSG1 = "Did you get the cake?";
    private static final String MSG2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String MSG3 = "Yohoooo, I am at your gate.";
    private static final String MSG4 = "It is dinner time!";
    private static final String MSG5 = "Ok, I am leaving without you.";

    private MessageManager manager;

    // A temporary folder keeps the tests away from the real messages.json
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        manager = new MessageManager("Test User", new File(tempFolder.getRoot(), "messages.json").getPath());
        manager.submitMessage("+27834557896", MSG1, 1);
        manager.submitMessage("+27838884567", MSG2, 3);
        manager.submitMessage("+27834484567", MSG3, 2);
        manager.submitMessage("0838884567", MSG4, 1);
        manager.submitMessage("+27838884567", MSG5, 3);
    }

    @Test
    public void sentMessagesArrayCorrectlyPopulated() {
        assertEquals(Arrays.asList(MSG1, MSG4), MessageManager.extractTexts(manager.getSentMessages()));
    }

    @Test
    public void disregardedMessagesArrayCorrectlyPopulated() {
        assertEquals(Arrays.asList(MSG3), MessageManager.extractTexts(manager.getDisregardedMessages()));
    }

    @Test
    public void storedMessagesArrayPopulatedFromJsonFile() {
        manager.getStoredMessages().clear();               // empty the array...
        int loaded = manager.loadStoredMessagesFromFile(); // ...then refill it from messages.json
        assertEquals(2, loaded);
        assertEquals(Arrays.asList(MSG2, MSG5), MessageManager.extractTexts(manager.getStoredMessages()));
    }

    @Test
    public void messageIdAndHashArraysContainAllMessages() {
        assertEquals(5, manager.getMessageIds().size());
        assertEquals(5, manager.getMessageHashes().size());
        assertEquals(manager.getSentMessages().get(0).getMessageId(), manager.getMessageIds().get(0));
        assertTrue(manager.getMessageHashes().get(0).matches("\\d{2}:0:DIDCAKE"));
        assertTrue(manager.getMessageHashes().get(3).matches("\\d{2}:3:ITTIME"));
    }

    @Test
    public void displayTheLongestMessage() {
        assertEquals(MSG2, manager.getLongestStoredMessage());
    }

    @Test
    public void searchForMessageId() {
        String idOfMessage4 = manager.getSentMessages().get(1).getMessageId();
        Message found = manager.searchByMessageId(idOfMessage4);
        assertNotNull(found);
        assertEquals("0838884567", found.getRecipientNumber());
        assertEquals(MSG4, found.getMessageText());
    }

    @Test
    public void searchForMessageIdThatDoesNotExist() {
        assertNull(manager.searchByMessageId("0000000000x"));
    }

    @Test
    public void searchAllMessagesForParticularRecipient() {
        assertEquals(Arrays.asList(MSG2, MSG5), manager.searchByRecipient("+27838884567"));
    }

    @Test
    public void deleteMessageUsingMessageHash() {
        String hashOfMessage2 = manager.getStoredMessages().get(0).getMessageHash();
        String result = manager.deleteByHash(hashOfMessage2);

        assertEquals("Message: \"" + MSG2 + "\" successfully deleted.", result);
        assertEquals(Arrays.asList(MSG5), MessageManager.extractTexts(manager.getStoredMessages()));
        assertFalse(manager.getMessageHashes().contains(hashOfMessage2));

        // The JSON file must be updated too
        manager.loadStoredMessagesFromFile();
        assertEquals(1, manager.getStoredMessages().size());
    }

    @Test
    public void deleteMessageWithUnknownHash() {
        assertEquals("No stored message was found with the hash 00:0:NOPE.", manager.deleteByHash("00:0:NOPE"));
        assertEquals(2, manager.getStoredMessages().size());
    }

    @Test
    public void displayReportListsHashRecipientAndMessageOfAllSentMessages() {
        String report = manager.generateSentReport();
        for (Message m : manager.getSentMessages()) {
            assertTrue(report.contains(m.getMessageHash()));
            assertTrue(report.contains(m.getRecipientNumber()));
            assertTrue(report.contains(m.getMessageText()));
        }
        assertFalse(report.contains(MSG2)); // stored messages are not part of the sent report
    }

    @Test
    public void storedReportListsFullDetailsOfAllStoredMessages() {
        String report = manager.generateStoredReport();
        for (Message m : manager.getStoredMessages()) {
            assertTrue(report.contains(m.getMessageId()));
            assertTrue(report.contains(m.getMessageHash()));
            assertTrue(report.contains((CharSequence) m.getSender()));
            assertTrue(report.contains(m.getRecipientNumber()));
            assertTrue(report.contains(m.getMessageText()));
        }
    }

    @Test
    public void displaySenderAndRecipientOfStoredMessages() {
        String expected = "Sender: Test User | Recipient: +27838884567\n"
                        + "Sender: Test User | Recipient: +27838884567\n";
        assertEquals(expected, manager.displaySenderAndRecipient());
    }

    @Test
    public void messageLongerThan250CharactersIsRejected() {
        String tooLong = new String(new char[260]).replace('\0', 'a');
        assertNull(manager.submitMessage("+27834557896", tooLong, 1));
        assertEquals(5, manager.getTotalMessages());

        Message message = new Message(6, "+27834557896", tooLong);
        assertEquals("Message exceeds 250 characters by 10, please reduce the size.", message.SentMessage(1));
    }

    @Test
    public void recipientNumbersOfTestDataAreValid() {
        for (Message m : manager.getSentMessages()) {
            assertEquals("Cell phone number successfully captured.", m.checkRecipientCell());
        }
    }

    @Test
    public void messageWithQuotesSurvivesJsonRoundTrip() {
        manager.submitMessage("+27834557896", "She said \"hello\" to me", 3);
        manager.getStoredMessages().clear();
        manager.loadStoredMessagesFromFile();
        assertTrue(MessageManager.extractTexts(manager.getStoredMessages()).contains("She said \"hello\" to me"));
    }
}
