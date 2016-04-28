package fr.upem.matou.hmi;

import javax.swing.*;
import java.awt.*;

/**
 * Represents all dialogs present in the application.
 *
 * @author Damien Chesneau
 */
class HmiTools {

    static void showError(String message) {
        showError(null, message);
    }

    static void showError(Component component, String message) {
        JOptionPane.showMessageDialog(component, message, "Info page", JOptionPane.ERROR_MESSAGE);
    }

    static void showInfo(Component component, String message) {
        JOptionPane.showMessageDialog(component, message, "Information page", JOptionPane.INFORMATION_MESSAGE);
    }

    public static int askPort(Component component) {
        return askPort(component, "Enter witch port you want to use.");
    }

    private static int askPort(Component component, String label) {
        String s = JOptionPane.showInputDialog(component, label, "Init server", JOptionPane.QUESTION_MESSAGE);
        if (s == null) {
            return -1;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return askPort(component, "Enter a valid port you want to use.");
        }
    }

    public static boolean doChoice(Component component, String pseudo, String title) {
        int i = JOptionPane.showConfirmDialog(component, pseudo, title, JOptionPane.YES_NO_OPTION);
        return i == 0;
    }
}
