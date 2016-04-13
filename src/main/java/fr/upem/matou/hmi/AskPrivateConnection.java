package fr.upem.matou.hmi;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.function.Consumer;

class AskPrivateConnection extends JDialog {

    private final Consumer<String> onAsk;

    private JPanel contentPane;
    private JButton buttonAsk;
    private JButton buttonCancel;
    private JTextField pseudoChoose;

    public AskPrivateConnection(Consumer<String> onAsk) {
        this.onAsk = Objects.requireNonNull(onAsk);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonAsk);
        buttonAsk.addActionListener((accessibleContext) -> onAsk());
        buttonCancel.addActionListener((accessibleContext) -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction((accessibleContext) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pack();
        setSize(330, 150);
        setTitle("Pseudo do contact");
        setResizable(false);
    }

    private void onAsk() {
        String text = pseudoChoose.getText();
        Objects.requireNonNull(text);//TODO Add graphical message.
        onAsk.accept(text);
        dispose();
    }

    private void onCancel() {
        dispose();
    }

}
