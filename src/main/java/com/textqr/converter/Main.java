package com.textqr.converter;

/**
 * Main launcher class to avoid JavaFX module system issues with shaded JAR.
 * This class serves as a wrapper to properly launch the JavaFX application.
 */
public class Main {
    public static void main(String[] args) {
        TextQRConverterApp.main(args);
    }
}