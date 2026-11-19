package chatapp;

import java.util.ArrayList;
import java.util.Scanner;

public class MainApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Login chatLoginSystem = new Login();
        boolean mainApplicationRunning = true;

        System.out.println("==================================================");
        System.out.println("         WELCOME TO THE CHAT APPLICATION          ");
        System.out.println("==================================================");
        System.out.print("Press ENTER to launch account registration... ");
        scanner.nextLine();

        // ------------------ PART 1: REGISTRATION ------------------
        System.out.println("\n--- ACCOUNT REGISTRATION ---");
        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter Last Name: ");
        String lastName = scanner.nextLine();
        System.out.print("Choose Username (Must contain '_' and be <= 5 chars): ");
        String username = scanner.nextLine();
        System.out.print("Set Password (Min 8 chars, 1 Uppercase, 1 Digit, 1 Special): ");
        String password = scanner.nextLine();
        System.out.print("Enter Cell Phone Number: ");
        String cellNo = scanner.nextLine();

        String registerResponse = chatLoginSystem.registerUser(username, password, firstName, lastName, cellNo);
        System.out.println("\n[SYSTEM RESPONSE]:\n" + registerResponse);

        if (registerResponse.contains("incorrectly formatted") || registerResponse.contains("not correctly formatted")) {
            System.out.println("\n[FATAL ERROR]: Registration parameters invalid. Exiting application.");
            scanner.close();
            return;
        }

        // ------------------ PART 1: LOGIN ------------------
        System.out.println("\n--- USER LOGIN AUTHORIZATION ---");
        System.out.print("Username: ");
        String inputtedUser = scanner.nextLine();
        System.out.print("Password: ");
        String inputtedPass = scanner.nextLine();

        boolean isAuthorized = chatLoginSystem.loginUser(inputtedUser, inputtedPass);
        String loginMessage = chatLoginSystem.returnLoginStatus(isAuthorized);
        System.out.println("\n[SYSTEM RESPONSE]: " + loginMessage);

        if (!isAuthorized) {
            System.out.println("\n[ACCESS DENIED]: Access blocked due to invalid login credentials.");
            scanner.close();
            return;
        }

        // ------------------ PART 3: SET UP ARRAYS ------------------
        // The manager owns the Sent, Disregarded, Stored, Hash and ID arrays.
        MessageManager manager = new MessageManager(chatLoginSystem.getFullName());
        int previouslyStored = manager.loadStoredMessagesFromFile();

        // ------------------ PART 2: SYSTEM MENUS ------------------
        System.out.println("\nWelcome to QuickChat.");
        if (previouslyStored > 0) {
            System.out.println("[INFO]: " + previouslyStored + " stored message(s) loaded from messages.json.");
        }

        while (mainApplicationRunning) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.print("Select menu option (1-4): ");

            String entrySelection = scanner.nextLine();

            switch (entrySelection) {
                case "1":
                    sendMessages(scanner, manager);
                    break;

                case "2":
                    System.out.println("\n" + manager.generateSentReport());
                    break;

                case "3":
                    System.out.println("Exiting application workspace. Goodbye.");
                    mainApplicationRunning = false;
                    break;

                case "4":
                    storedMessagesMenu(scanner, manager);
                    break;

                default:
                    System.out.println("[ALERT]: Invalid menu selection. Use keys 1-4.");
                    break;
            }
        }
        scanner.close();
    }

    // ------------------ MENU OPTION 1: SEND MESSAGES ------------------
    private static void sendMessages(Scanner scanner, MessageManager manager) {
        System.out.print("How many messages do you wish to enter? ");
        int batchSize = readInt(scanner, 1);

        for (int i = 0; i < batchSize; i++) {
            int messageNumber = manager.getNextMessageNumber();
            System.out.println("\nEntering details for Message Execution Check #" + messageNumber);

            System.out.print("Enter Recipient Number: ");
            String recipientCell = scanner.nextLine();

            System.out.println("Please enter a message of less than 250 characters.");
            System.out.print("Enter Message Content: ");
            String content = scanner.nextLine();

            Message message = new Message(messageNumber, recipientCell, content, manager.getSender());

            System.out.println("Select Delivery Processing Action Option:");
            System.out.println(" 1 - Send Message\n 2 - Disregard Message\n 3 - Store Message");
            System.out.print("Choice: ");
            int choice = readInt(scanner, 1);

            System.out.println("\n[CELL CHECKS]: " + message.checkRecipientCell());

            String deliveryFeedback = message.SentMessage(choice);
            System.out.println("[STATUS ALERTS]: " + deliveryFeedback);

            if (deliveryFeedback.contains("Message exceeds 250 characters")) {
                System.out.println("[REJECTED]: Message was not saved due to structural length overflow.");
            } else if (manager.addMessage(message, choice)) {
                System.out.println("\n--- DISPLAYING FULL DISPATCH DATA METADATA ---");
                System.out.println("Message ID: " + message.getMessageId());
                System.out.println("Message Hash: " + message.getMessageHash());
                System.out.println("Recipient: " + message.getRecipientNumber());
                System.out.println("Message: " + message.getMessageText());
                System.out.println("------------------------------------------------");
                if (choice == 3) {
                    System.out.println("[JSON STORAGE SYNCED]: Message written to messages.json.");
                }
            } else {
                System.out.println("[REJECTED]: Unknown action choice, message was not recorded.");
            }
        }

        System.out.println("\n[LOG SUMMARY]: Total number of messages accumulated: " + manager.getTotalMessages());
    }

    // ------------------ MENU OPTION 4: STORED MESSAGES ------------------
    private static void storedMessagesMenu(Scanner scanner, MessageManager manager) {
        // Read the JSON file back into the stored messages array each time the menu opens
        manager.loadStoredMessagesFromFile();

        boolean inStoredMenu = true;
        while (inStoredMenu) {
            System.out.println("\n--- STORED MESSAGES ---");
            System.out.println("1) Display sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search for all messages for a particular recipient");
            System.out.println("5) Delete a message using the message hash");
            System.out.println("6) Display a report of all stored messages");
            System.out.println("7) Back to main menu");
            System.out.print("Select option (1-7): ");

            String selection = scanner.nextLine();

            switch (selection) {
                case "1":
                    System.out.println("\n" + manager.displaySenderAndRecipient());
                    break;

                case "2":
                    System.out.println("\nLongest stored message: " + manager.getLongestStoredMessage());
                    break;

                case "3":
                    System.out.print("Enter the message ID: ");
                    Message found = manager.searchByMessageId(scanner.nextLine().trim());
                    if (found == null) {
                        System.out.println("No message found with that ID.");
                    } else {
                        System.out.println("Recipient: " + found.getRecipientNumber());
                        System.out.println("Message: " + found.getMessageText());
                    }
                    break;

                case "4":
                    System.out.print("Enter the recipient number: ");
                    String recipient = scanner.nextLine().trim();
                    ArrayList<String> matches = manager.searchByRecipient(recipient);
                    if (matches.isEmpty()) {
                        System.out.println("No messages found for " + recipient + ".");
                    } else {
                        System.out.println("Messages for " + recipient + ":");
                        for (String text : matches) {
                            System.out.println(" - " + text);
                        }
                    }
                    break;

                case "5":
                    System.out.print("Enter the message hash: ");
                    System.out.println(manager.deleteByHash(scanner.nextLine().trim()));
                    break;

                case "6":
                    System.out.println("\n" + manager.generateStoredReport());
                    break;

                case "7":
                    inStoredMenu = false;
                    break;

                default:
                    System.out.println("[ALERT]: Invalid selection. Use keys 1-7.");
                    break;
            }
        }
    }

    // Reads a whole number from the console, falling back to a default if the input is not a number
    private static int readInt(Scanner scanner, int defaultValue) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR]: Invalid format. Defaulting to " + defaultValue + ".");
            return defaultValue;
        }
    }
}
