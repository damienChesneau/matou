package fr.upem.matou.hmi;

import fr.upem.matou.client.IOConsumer;
import fr.upem.matou.client.Info;
import fr.upem.matou.common.Message;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Damien Chesneau
 */
public class PrivateConnection extends JFrame {
    private final String targetPseudo;
    private final MessagesTableModel messagesTableModel;
    private final IOConsumer<Path> sendFile;
    private final IOConsumer<String> sendMessage;
    private JTable tableMessages;
    private JButton buttonSendMessage;
    private JTextArea writeMessage;
    private JButton sendFileButton;
    private JLabel connectedLabel;
    private JPanel root;

    PrivateConnection(String targetPseudo, IOConsumer<Path> sendFile) {
        this(null, targetPseudo, sendFile);
    }

    PrivateConnection(IOConsumer<String> sendMessage, String targetPseudo, IOConsumer<Path> sendFile) {
        this.targetPseudo = Objects.requireNonNull(targetPseudo);
        this.sendFile = (sendFile);
        this.sendMessage = (sendMessage);
        connectedLabel.setText(targetPseudo + " chat");
        setContentPane(root);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        messagesTableModel = new MessagesTableModel();
        tableMessages.setModel(messagesTableModel);
        buttonSendMessage.addActionListener(this::onListenerNewMessage);
        sendFileButton.addActionListener(this::onListenerSendFile);
        setSize(620, 450);
        setTitle("Private chat");
    }

    public void newMessage(String message) {
        Objects.requireNonNull(message);
        messagesTableModel.newMessage(new Message(targetPseudo, message));
    }

    public void onError(Info info) {
        Objects.requireNonNull(info);
        switch (info) {
            case CLIENT_ACCEPT_FILE:
                HmiTools.showInfo(this, "Remote client accept the transfer.");
                break;
            case CLIENT_REFUSE_FILE:
                HmiTools.showInfo(this, "Remote client refuse the transfer.");
                break;
            case ILLEGAL_SECURE_NUMBER:
                HmiTools.showInfo(this, "We can't establish the connection.");
                break;
            case TRANSFER_COMPLETED:
                HmiTools.showInfo(this, "File transfer finish.");
                break;
            default:
                throw new AssertionError();
        }
    }

    private void onListenerSendFile(ActionEvent l) {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.showDialog(this, "Choose our file to send");
        Path path = jFileChooser.getSelectedFile().toPath();
        try {
            sendFile.accept(path);
        } catch (IOException e) {
            HmiTools.showError(this, "Can't send file request.");
        }
    }

    private void onListenerNewMessage(ActionEvent l) {
        String text = writeMessage.getText();
        writeMessage.setText("");
        messagesTableModel.newMessage(new Message("My self", text));
        try {
            sendMessage.accept(text);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            HmiTools.showError(e.getMessage());
        }
    }

    boolean askIfUserWhatAskedFile(String fileName) {
        return HmiTools.doChoice(this, "Do you want to get " + fileName + " file ? ", "Remote user wan't to send you a file.");
    }

}
