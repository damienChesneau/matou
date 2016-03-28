package fr.upem.matou;

import fr.upem.matou.hmi.Chat;

import javax.swing.*;
import java.io.IOException;

/**
 * @author Damien Chesneau
 */
class Main {
    private Main() {
        //empty
    }

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        Chat c = new Chat();
        c.setVisible(true);
    }
}
