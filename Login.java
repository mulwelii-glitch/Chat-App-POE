package chatapp;

import java.util.regex.Pattern;

public class Login {
    // Declaring private instance variables to securely store registered user credentials
    private String registeredUsername;
    private String registeredPassword;
    private String registeredFirstName;
    private String registeredLastName;
    private String registeredCellNumber;

    
    public boolean checkUserName(String username) {
        if (username == null) {
            return false;
        }
        return username.contains("_") && username.length() <= 5;
    }

        public boolean checkPasswordComplexity(String password) {
        if (password == null) {
            return false;
        }
        
        boolean hasMinimumLength = password.length() >= 8;
        boolean hasUppercase = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecialChar = Pattern.compile("[^a-zA-Z0-9]").matcher(password).find();

        return hasMinimumLength && hasUppercase && hasDigit && hasSpecialChar;
    }

   
    public boolean checkCellPhoneNumber(String cellNumber) {
        if (cellNumber == null) {
            return false;
        }
       
        String southAfricanMobileRegex = "^\\+27\\d{9}$";
        return Pattern.matches(southAfricanMobileRegex, cellNumber);
    }

   
    public String registerUser(String username, String password, String firstName, String lastName, String cellNumber) {
        if (!checkUserName(username)) {
            return "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
        }
        
        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        }

        if (!checkCellPhoneNumber(cellNumber)) {
            return "Cell number is incorrectly formatted or does not contain an international code; please correct the number and try again.";
        }

        // Persist successful parameters into state fields
        this.registeredUsername = username;
        this.registeredPassword = password;
        this.registeredFirstName = firstName;
        this.registeredLastName = lastName;
        this.registeredCellNumber = cellNumber;

        return "Username successfully captured.\nPassword successfully captured.\nCell number successfully captured.";
    }

    
    public boolean loginUser(String username, String password) {
        if (this.registeredUsername == null || this.registeredPassword == null) {
            return false;
        }
        return this.registeredUsername.equals(username) && this.registeredPassword.equals(password);
    }

   
    public String returnLoginStatus(boolean loginSuccess) {
        if (loginSuccess) {
            return "Welcome " + this.registeredFirstName + " " + this.registeredLastName + ", it is great to see you again.";
        } else {
            return "Username or password incorrect, please try again.";
        }
    }

     public String getFullName() {
        return this.registeredFirstName + " " + this.registeredLastName;
     }
}
