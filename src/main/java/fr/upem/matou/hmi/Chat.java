package fr.upem.matou.hmi;

import fr.upem.matou.client.Client;
import fr.upem.matou.client.Info;
import fr.upem.matou.client.PrivateConnectionChat;
import fr.upem.matou.common.Message;
import fr.upem.matou.server.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * Main dialog.
 *
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
    private JLabel srvInfo;
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
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
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
        if (server == null) {
            int port = HmiTools.askPort(this);
            if (port == -1) {
                return;
            }
            new Thread(() -> {
                try {
                    server = Server.init(port);
                    srvInfo.setText("Srv started on " + port);
                    initializeServerButton.setText("Stop server");
                    server.launch();
                } catch (BindException e) {
                    JOptionPane.showMessageDialog(component, "Port already in use.", "Info", JOptionPane.ERROR_MESSAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println(e);
                }
            }).start();
        } else {
            try {
                server.close();
                srvInfo.setText("");
                initializeServerButton.setText("init local server");
            } catch (IOException e) {
                onError(Info.CLIENT_REFUSE_FILE);//TODO
            }
        }
    }

    private String getMessageWritten() {
        String text = writeMessage.getText();
        writeMessage.setText("");
        return text;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        root = new JPanel();
        root.setLayout(new BorderLayout(0, 0));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        root.add(panel1, BorderLayout.WEST);
        final JPanel spacer1 = new JPanel();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer2, gbc);
        labelPseudo = new JLabel();
        labelPseudo.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(labelPseudo, gbc);
        connectAServerButton = new JButton();
        connectAServerButton.setText("Connect a server");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(connectAServerButton, gbc);
        initializeServerButton = new JButton();
        initializeServerButton.setText("init local server");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel1.add(initializeServerButton, gbc);
        buttonCreatePrivateConn = new JButton();
        buttonCreatePrivateConn.setText("Ask private chat");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(buttonCreatePrivateConn, gbc);
        srvInfo = new JLabel();
        srvInfo.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(srvInfo, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel2.setPreferredSize(new Dimension(435, 500));
        panel2.setRequestFocusEnabled(true);
        root.add(panel2, BorderLayout.EAST);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        panel3.setPreferredSize(new Dimension(450, 300));
        panel3.setRequestFocusEnabled(false);
        panel2.add(panel3, BorderLayout.NORTH);
        table1 = new JTable();
        panel3.add(table1, BorderLayout.CENTER);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        panel4.setPreferredSize(new Dimension(450, 100));
        panel4.setRequestFocusEnabled(false);
        panel2.add(panel4, BorderLayout.SOUTH);
        buttonSendMessage = new JButton();
        buttonSendMessage.setLabel("Send");
        buttonSendMessage.setText("Send");
        panel4.add(buttonSendMessage, BorderLayout.EAST);
        writeMessage = new JTextArea();
        panel4.add(writeMessage, BorderLayout.CENTER);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new BorderLayout(0, 0));
        panel2.add(panel5, BorderLayout.CENTER);
        final JLabel label1 = new JLabel();
        label1.setText("Enter our message in this box :");
        panel5.add(label1, BorderLayout.CENTER);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root;
    }
}
