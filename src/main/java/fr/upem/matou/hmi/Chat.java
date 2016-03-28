package fr.upem.matou.hmi;

import fr.upem.matou.client.Client;
import fr.upem.matou.client.Info;
import fr.upem.matou.client.PrivateConnectionChat;
import fr.upem.matou.common.Message;
import fr.upem.matou.server.Server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author Damien Chesneau
 */
public class Chat extends JFrame {
    private final MessagesTableModel model = new MessagesTableModel();

    private JButton initializeServerButton;
    private JPanel root;
    private JButton buttonSendMessage;
    private JLabel labelPseudo;
    private JTextArea writeMessage;
    private JTable table1;
    private JButton connectAServerButton;
    private JButton buttonCreatePrivateConn;
    private Client client;
    private Server server;

    public Chat() {
        initializeServerButton.addActionListener(this::initServer);
        buttonSendMessage.addActionListener(this::newMessage);
        connectAServerButton.addActionListener(e -> {
            ConnectServer connectServer = new ConnectServer(this::connectToServerDialog);
            connectServer.open();
        });
        buttonCreatePrivateConn.addActionListener(this::askForPrivateConnection);
        writeMessage.setEditable(false);
        buttonSendMessage.setVisible(false);
        buttonCreatePrivateConn.setVisible(false);
        setContentPane(root);
        table1.setModel(model);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setSize(620, 450);
        setTitle("Global chat");
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    if (server != null) {
                        server.close();
                    }
                    if (client != null) {
                        client.close();
                    }
                } catch (IOException e) {
                    System.err.println("Can't close the server.");
                }
            }
        });
    }

    private void askForPrivateConnection(ActionEvent l) {
        AskPrivateConnection dialog = new AskPrivateConnection(this::actionOnClientForAskPrivate);
        dialog.setVisible(true);
    }


    private void actionOnClientForAskPrivate(String login) {
        if (client != null) {
            try {
                client.askPrivateConnection(login);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onError(Info info) {
        switch (info) {
            case ILLEGAL_SECURE_NUMBER:
                HmiTools.showError("Can't establish connection under clients secure number wasn't the same.");
        }
    }

    private void onNewPrivateConnectionAlreadyAccepted(PrivateConnectionChat source) {
        PrivateConnection privateConnection = new PrivateConnection(source::sendMessage, source.getPseudo(), source::sendRequestForFileTransfer);
        try {
            source.processAsSource(privateConnection::newMessage, privateConnection::onError, privateConnection::askIfUserWhatAskedFile);
            privateConnection.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectToServerDialog(String host, int port, String pseudo) {
        try {
            this.client = Client.newInstance(pseudo, new InetSocketAddress(host, port));
            if (client.connect(this::notifyModel, this::incomingPrivateConnectionRequest, this::onNewPrivateConnectionAlreadyAccepted)) {
                writeMessage.setEditable(true);
                buttonSendMessage.setVisible(true);
                buttonCreatePrivateConn.setVisible(true);
                labelPseudo.setText("Connected as : " + pseudo);
            }
        } catch (IOException e) {
            ioExceptionManaging(e);
        }
    }

    private void notifyModel(Message message) {
        model.newMessage(message);
    }

    private void incomingPrivateConnectionRequest(String pseudo) {
        Objects.requireNonNull(client);
        try {
            if (HmiTools.doChoice(this, pseudo + " want's to talk with you, do you accept ? ", "Ask for a private connection.")) {
                PrivateConnectionChat target = client.validatePrivateConnectionWith(pseudo);
                PrivateConnection privateConnection = new PrivateConnection(target::sendMessage, target.getPseudo(), target::sendRequestForFileTransfer);
                Thread t = new Thread(() -> {
                    try {
                        target.processAsTarget(privateConnection::newMessage, privateConnection::onError, privateConnection::askIfUserWhatAskedFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                t.start();
                privateConnection.setVisible(true);
            } else {
                client.refusePrivateConnectionWith();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void newMessage(ActionEvent l) {
        if (client != null) {
            try {
                String messageWritten = getMessageWritten();
                client.sendMessage(messageWritten);
            } catch (IOException e) {
                ioExceptionManaging(e);
            }
        }
    }

    private void ioExceptionManaging(IOException e) {
        HmiTools.showError(Chat.this, "An communication error was occurred.");
        System.err.println(e);
        e.printStackTrace();
    }

    private void initServer(ActionEvent l) {
        Chat component = this;
        int port = HmiTools.askPort(this);
        new Thread(() -> {
            try {
                server = Server.init(port);
                server.launch();
            } catch (BindException e) {
                JOptionPane.showMessageDialog(component, "Port already in use.", "Info", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println(e);
            }
        }).start();
    }

    private String getMessageWritten() {
        String text = writeMessage.getText();
        writeMessage.setText("");
        return text;
    }

}
