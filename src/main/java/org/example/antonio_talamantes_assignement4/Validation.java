package org.example.antonio_talamantes_assignement4;

public class Validation {

    public static String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) { // Check if title ir null or empty
            return "Title cannot be empty";     // If empty, return why error message
        }
        if (!title.matches("^[A-Z][a-zA-Z0-9\\-\\:\\.\\s]*$")) {
            /* 1. Put a ^ to indicate the start of the string,
                    this makes sure the string starts in the beginning because otherwise it will accept
                    any string that contains the pattern even if it doesn't start with [A-Z].

               2. Then [A-Z] first character must be a capital letter
               3. Then [a-zA-Z0-9\\-\\:\\.\\s]* rest of string any letter, number, hyphen, colon, period, or space
                    I was going use \\w instead of [a-zA-Z0-9\\-\\:\\.\\s] but it also includes underscores
               4. * for 0 or more characters
               5. $ this means the string has ended so will not accept any more characters
             */
            return "Title must start with a capital letter and can only contain letters, numbers, hyphens, colons, periods, or spaces";

        }
        return ""; // If no errors, return empty string
    }

    public static String validateYear(String year) {
        /* Using String because I don't need to do any math on the year
           and I can use the matches method to check if the string is a number
         */
        if (year == null || year.trim().isEmpty()) { // Check if year ir null or empty
            return "Year cannot be empty";     // If empty, return why error message
        }
        if (!year.matches("^[0-9]{4}$")) {
            return "Year must contain 4 digits";
        }
        return ""; // If no errors, return empty string
    }

    public static String validateSales(String sales) {
        if (sales == null || sales.trim().isEmpty()) { // Check if sales ir null or empty
            return "Sales cannot be empty";     // If empty, return why error message
        }
        try {
            Double.parseDouble(sales); // Try to parse the string to a double

        } catch (NumberFormatException e) {
            return "Sales can only contain digits. The decimal point is optional. If the decimal point is included there must be at least one number before and at least one number after it.";
        }
        return ""; // If no errors, return empty string
    }
}
