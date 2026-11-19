package chatapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Part 3: holds every array required by the task and all the operations on them.
 *
 * Arrays (all populated at runtime, nothing hard-coded):
 *   sentMessages, disregardedMessages, storedMessages, messageHashes, messageIds
 *
 * Attribution: this class was written with assistance from Claude (Anthropic, 2026).
 * The JSON string-escape rules used by the reader/writer follow the JSON specification (https://www.json.org).
 */
public class MessageManager {
    public static final String DEFAULT_STORAGE_FILE = "messages.json";

    private final ArrayList<Message> sentMessages = new ArrayList<>();
    private final ArrayList<Message> disregardedMessages = new ArrayList<>();
    private final ArrayList<Message> storedMessages = new ArrayList<>();
    private final ArrayList<String> messageHashes = new ArrayList<>();
    private final ArrayList<String> messageIds = new ArrayList<>();

    private final String sender;
    private final String storageFile;
    private int messageCounter = 0;

    public MessageManager(String sender) {
        this(sender, DEFAULT_STORAGE_FILE);
    }

    public MessageManager(String sender, String storageFile) {
        this.sender = sender;
        this.storageFile = storageFile;
    }

    // ------------------------------------------------------------------
    // Populating the arrays
    // ------------------------------------------------------------------

    /** The number the next accepted message will receive (used for the message hash). */
    public int getNextMessageNumber() {
        return messageCounter + 1;
    }

    /**
     * Places a message in the correct array using the flag chosen by the user:
     * 1 = Sent, 2 = Disregard, 3 = Store. Returns false for any other flag.
     */
    public boolean addMessage(Message message, int flag) {
        switch (flag) {
            case 1:
                sentMessages.add(message);
                break;
            case 2:
                disregardedMessages.add(message);
                break;
            case 3:
                storedMessages.add(message);
                saveStoredMessagesToFile();
                break;
            default:
                return false;
        }
        messageIds.add(message.getMessageId());
        messageHashes.add(message.getMessageHash());
        messageCounter++;
        return true;
    }

    /** Convenience method: builds a message from raw data and files it (used to load the test data). */
    public Message submitMessage(String recipient, String text, int flag) {
        if (text == null || text.length() > 250) {
            return null;
        }
        Message message = new Message(getNextMessageNumber(), recipient, text, sender);
        return addMessage(message, flag) ? message : null;
    }

    // ------------------------------------------------------------------
    // JSON storage (write + read back into the storedMessages array)
    // ------------------------------------------------------------------

    /** Writes the storedMessages array to the JSON file. */
    public boolean saveStoredMessagesToFile() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            json.append(storedMessages.get(i).storeMessage());
            if (i < storedMessages.size() - 1) {
                json.append(",\n");
            }
        }
        json.append("\n]");
        try {
            Files.write(Paths.get(storageFile), json.toString().getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            System.out.println("[CRITICAL WRITE ERROR]: IO storage failure.");
            return false;
        }
    }

    /**
     * Reads the JSON file back into the storedMessages array.
     * Returns how many stored messages were loaded (0 if the file does not exist yet).
     */
    public int loadStoredMessagesFromFile() {
        Path path = Paths.get(storageFile);
        if (!Files.exists(path)) {
            return 0;
        }
        try {
            String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            ArrayList<Message> loaded = parseJson(json);

            storedMessages.clear();
            storedMessages.addAll(loaded);

            // Make sure the ID and hash arrays also know about messages stored in an earlier session
            for (Message m : loaded) {
                if (!messageIds.contains(m.getMessageId())) {
                    messageIds.add(m.getMessageId());
                }
                if (!messageHashes.contains(m.getMessageHash())) {
                    messageHashes.add(m.getMessageHash());
                }
            }
            return loaded.size();
        } catch (IOException e) {
            System.out.println("[READ ERROR]: Could not read the stored messages file.");
            return 0;
        }
    }

    // Small hand-written reader for the file format produced by Message.storeMessage()
    private static ArrayList<Message> parseJson(String json) {
        ArrayList<Message> result = new ArrayList<>();
        Map<String, String> fields = null;
        String pendingKey = null;
        int i = 0;

        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '{') {
                fields = new HashMap<>();
                pendingKey = null;
                i++;
            } else if (c == '}') {
                if (fields != null) {
                    result.add(new Message(
                            fields.getOrDefault("messageId", ""),
                            fields.getOrDefault("messageHash", ""),
                            fields.getOrDefault("sender", "Unknown"),
                            fields.getOrDefault("recipient", ""),
                            fields.getOrDefault("message", "")));
                }
                fields = null;
                i++;
            } else if (c == '"') {
                StringBuilder value = new StringBuilder();
                i++;
                while (i < json.length() && json.charAt(i) != '"') {
                    char ch = json.charAt(i);
                    if (ch == '\\' && i + 1 < json.length()) {
                        i++;
                        char esc = json.charAt(i);
                        switch (esc) {
                            case 'n': value.append('\n'); break;
                            case 'r': value.append('\r'); break;
                            case 't': value.append('\t'); break;
                            case 'u':
                                if (i + 4 < json.length()) {
                                    value.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                                    i += 4;
                                }
                                break;
                            default: value.append(esc);
                        }
                    } else {
                        value.append(ch);
                    }
                    i++;
                }
                i++; // skip closing quote
                if (fields != null) {
                    if (pendingKey == null) {
                        pendingKey = value.toString();
                    } else {
                        fields.put(pendingKey, value.toString());
                        pendingKey = null;
                    }
                }
            } else {
                i++;
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // Stored Messages menu features
    // ------------------------------------------------------------------

    /** (a) Sender and recipient of every stored message. */
    public String displaySenderAndRecipient() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (Message m : storedMessages) {
            sb.append("Sender: ").append(m.getSender())
              .append(" | Recipient: ").append(m.getRecipientNumber()).append("\n");
        }
        return sb.toString();
    }

    /** (b) Text of the longest stored message. */
    public String getLongestStoredMessage() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        Message longest = storedMessages.get(0);
        for (Message m : storedMessages) {
            if (m.getMessageText().length() > longest.getMessageText().length()) {
                longest = m;
            }
        }
        return longest.getMessageText();
    }

    /** (c) Finds a message by its ID in the sent, disregarded and stored arrays; null if not found. */
    public Message searchByMessageId(String id) {
        for (Message m : sentMessages) {
            if (m.getMessageId().equals(id)) return m;
        }
        for (Message m : disregardedMessages) {
            if (m.getMessageId().equals(id)) return m;
        }
        for (Message m : storedMessages) {
            if (m.getMessageId().equals(id)) return m;
        }
        return null;
    }

    /** (d) Text of every sent or stored message for one recipient. */
    public ArrayList<String> searchByRecipient(String recipient) {
        ArrayList<String> matches = new ArrayList<>();
        for (Message m : sentMessages) {
            if (m.getRecipientNumber().equals(recipient)) matches.add(m.getMessageText());
        }
        for (Message m : storedMessages) {
            if (m.getRecipientNumber().equals(recipient)) matches.add(m.getMessageText());
        }
        return matches;
    }

    /** (e) Deletes a stored message using its hash and updates the ID/hash arrays and the JSON file. */
    public String deleteByHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            if (m.getMessageHash().equalsIgnoreCase(hash)) {
                storedMessages.remove(i);
                messageHashes.remove(m.getMessageHash());
                messageIds.remove(m.getMessageId());
                saveStoredMessagesToFile();
                return "Message: \"" + m.getMessageText() + "\" successfully deleted.";
            }
        }
        return "No stored message was found with the hash " + hash + ".";
    }

    /** (f) Full report of all stored messages. */
    public String generateStoredReport() {
        return buildReport("STORED MESSAGES REPORT", storedMessages);
    }

    /** Report of all sent messages (used by main menu option 2). */
    public String generateSentReport() {
        return buildReport("SENT MESSAGES REPORT", sentMessages);
    }

    private String buildReport(String title, ArrayList<Message> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("========== ").append(title).append(" ==========\n");
        if (messages.isEmpty()) {
            sb.append("No messages to display.\n");
        }
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            sb.append("Message ").append(i + 1).append("\n")
              .append("  Message ID   : ").append(m.getMessageId()).append("\n")
              .append("  Message Hash : ").append(m.getMessageHash()).append("\n")
              .append("  Sender       : ").append(m.getSender()).append("\n")
              .append("  Recipient    : ").append(m.getRecipientNumber()).append("\n")
              .append("  Message      : ").append(m.getMessageText()).append("\n");
        }
        sb.append("=============================================\n");
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Accessors (the tests use these to inspect the arrays)
    // ------------------------------------------------------------------
    public ArrayList<Message> getSentMessages() { return sentMessages; }
    public ArrayList<Message> getDisregardedMessages() { return disregardedMessages; }
    public ArrayList<Message> getStoredMessages() { return storedMessages; }
    public ArrayList<String> getMessageHashes() { return messageHashes; }
    public ArrayList<String> getMessageIds() { return messageIds; }
    public int getTotalMessages() { return messageCounter; }
    public String getSender() { return sender; }

    /** Returns just the text of each message in a list (handy for comparing against expected output). */
    public static ArrayList<String> extractTexts(ArrayList<Message> messages) {
        ArrayList<String> texts = new ArrayList<>();
        for (Message m : messages) {
            texts.add(m.getMessageText());
        }
        return texts;
    }
}
