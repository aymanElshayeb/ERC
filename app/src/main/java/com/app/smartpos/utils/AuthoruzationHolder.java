package com.app.smartpos.utils;

public class AuthoruzationHolder {
    // Define a ThreadLocal variable for a String, initialized with an empty string
    private static final ThreadLocal<String> authorization = ThreadLocal.withInitial(() -> "");

    // Getter for thread-local variable
    public static String getAuthorization() {
        return authorization.get();
    }

    // Setter for thread-local variable
    public static void setAuthorization(String value) {
        authorization.set(value);
    }

    // Reset method to clear the thread-local value and reinitialize it to an empty string
    public static void resetAuthorization() {
        authorization.remove(); // Removes the value for the current thread
    }
}
