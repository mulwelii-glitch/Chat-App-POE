package chatapp;

import java.util.Random;

/**
 * Represents a single chat message.
 * Part 3 additions: a sender field, a constructor for messages that are
 * re-loaded from the JSON file, and JSON-safe output in storeMessage().
 */
public class Message {
    private String messageId;
    private int messageNumber;
    private String sender;
    private String recipientNumber;
    private String messageText;
    private String messageHash;

    // Part 1/2 constructor (kept so existing code still works)
    public Message(int messageNumber, String recipientNumber, String messageText) {
        this(messageNumber, recipientNumber, messageText, "Unknown");
    }

    // Part 3 constructor: also records who sent the message
    public Message(int messageNumber, String recipientNumber, String messageText, String sender) {
        this.messageNumber = messageNumber;
        this.recipientNumber = recipientNumber;
        this.messageText = messageText;
        this.sender = sender;
        this.messageId = generateRandom10DigitID();
        this.messageHash = createMessageHash();
    }

    // Used when a message is read back from the JSON file (ID and hash already exist)
    public Message(String messageId, String messageHash, String sender,
                   String recipientNumber, String messageText) {
        this.messageId = messageId;
        this.messageHash = messageHash;
        this.sender = sender;
        this.recipientNumber = recipientNumber;
        this.messageText = messageText;
        this.messageNumber = 0;
    }

    private String generateRandom10DigitID() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public boolean checkMessageID() {
        return this.messageId != null && this.messageId.length() <= 10;
    }

    public String checkRecipientCell() {
        if (this.recipientNumber == null || this.recipientNumber.isEmpty()) {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }

        boolean isValidInternational = this.recipientNumber.startsWith("+27") && this.recipientNumber.length() == 12;
        boolean isValidLocal = this.recipientNumber.startsWith("0") && this.recipientNumber.length() == 10;

        if (isValidInternational || isValidLocal) {
            return "Cell phone number successfully captured.";
        }

        return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    public String createMessageHash() {
        if (this.messageId == null || this.messageId.length() < 2) {
            return "00:0:EMPTY";
        }

        String firstTwoNumbers = this.messageId.substring(0, 2);
        int zeroIndexedNumber = this.messageNumber - 1;

        String cleanText = this.messageText.trim();
        String[] words = cleanText.split("\\s+");

        String firstWord = words[0].replaceAll("[^a-zA-Z0-9]", "");
        String lastWord = words[words.length - 1].replaceAll("[^a-zA-Z0-9]", "");

        String compiledHash = firstTwoNumbers + ":" + zeroIndexedNumber + ":" + firstWord + lastWord;
        return compiledHash.toUpperCase();
    }

    public String SentMessage(int choice) {
        if (this.messageText != null && this.messageText.length() > 250) {
            int extraCharactersCount = this.messageText.length() - 250;
            return "Message exceeds 250 characters by " + extraCharactersCount + ", please reduce the size.";
        }

        switch (choice) {
            case 1:
                return "Message successfully sent.";
            case 2:
                return "Press 0 to delete the message.";
            case 3:
                return "Message successfully stored.";
            default:
                return "Unknown selection protocol choice.";
        }
    }

    /** Returns this message as one JSON object (string values are escaped per the JSON spec, json.org). */
    public String storeMessage() {
    return "  {\n" +
           "    \"messageId\": \"" + escapeJson(messageId) + "\",\n" +
           "    \"messageHash\": \"" + escapeJson(messageHash) + "\",\n" +
           "    \"sender\": \"" + escapeJson(sender) + "\",\n" +
           "    \"recipient\": \"" + escapeJson(recipientNumber) + "\",\n" +
           "    \"message\": \"" + escapeJson(messageText) + "\"\n" +
           "  }";
}
    
    // Escapes characters that would otherwise break the JSON file (quotes, backslashes, line breaks)
    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    // Getters
    public String getMessageId() { return messageId; }
    public String getMessageHash() { return messageHash; }
    public String getSender() { return sender; }
    public String getRecipientNumber() { return recipientNumber; }
    public String getMessageText() { return messageText; }
}
