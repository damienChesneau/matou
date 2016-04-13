package fr.upem.matou.hmi;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

class ConnectServer extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField host;
    private JTextField pseudo;
    private JTextField serverPort;
    private final ThreeConsumer onOk;

    interface ThreeConsumer {
        void consume(String host, int port, String pseudo);
    }

    ConnectServer(ThreeConsumer onOk) {
        this.onOk = Objects.requireNonNull(onOk);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pack();
        this.host.setText("localhost");
        setResizable(false);
        this.serverPort.setText("7777");
    }

    public void open() {
        setVisible(true);
    }

    private void onOK() {
        onOk.consume(host.getText(), ensureInt(), pseudo.getText());
        dispose();
    }

    private int ensureInt() {
        String text = serverPort.getText();
        try {
            return Integer.parseInt(text);
        }catch (NumberFormatException e){
            JOptionPane.showInputDialog(this, "Set the port as an int.", "Init server", JOptionPane.QUESTION_MESSAGE);
            return ensureInt();
        }
    }

    private void onCancel() {
        dispose();
    }
}
