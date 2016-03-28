package fr.upem.matou.common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author Damien Chesneau
 */
public class Querys {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static ByteBuffer encodeServerConnect(String login) {
        ByteBuffer encoded = UTF8.encode(login);
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + encoded.remaining() + Short.BYTES);
        bb.put(Query.CONNECT_SERVER.getOperationCode());
        bb.putShort((short) encoded.remaining());
        bb.put(encoded);
        return bb;
    }

    public static byte decodeOperationCode(ByteBuffer bb) {
        bb.flip();
        try {
            return bb.get();
        } finally {
            bb.compact();
        }
    }

    public static String decodeServerConnect(ByteBuffer bb) {
        bb.flip();
        short lenOfLogin = bb.getShort();
        return decodeString(bb, lenOfLogin);
    }

    public static ByteBuffer encodeSuccessResponse() {
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES);
        bb.put(Query.VALIDATE_CONNECTION.getOperationCode());
        return bb;
    }

    public static ByteBuffer encodeConnectionErrorResponse() {
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES);
        bb.put(Query.ERROR_CONNECTION.getOperationCode());
        return bb;
    }

    public static ByteBuffer encodeSendMessageToServer(String message) {
        ByteBuffer encoded = UTF8.encode(message);
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + encoded.remaining() + Integer.BYTES);
        bb.put(Query.SEND_SRV_MESSAGE.getOperationCode());
        bb.putInt(encoded.remaining());
        bb.put(encoded);
        return bb;
    }

    public static String decodeSendMessageToServer(ByteBuffer bb) {
        bb.flip();
        int lenOfMessage = bb.getInt();
        return decodeString(bb, lenOfMessage);
    }

    public static ByteBuffer encodeBroadcastMessage(String login, String message) {
        ByteBuffer loginEncoded = UTF8.encode(login);
        ByteBuffer messageEncoded = UTF8.encode(message);
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + loginEncoded.remaining() + messageEncoded.remaining() + Integer.BYTES);
        bb.put(Query.BROADCAST_CLIENTS_MESSAGE.getOperationCode());
        bb.putShort((short) loginEncoded.remaining());
        bb.put(loginEncoded);
        bb.putInt(messageEncoded.remaining());
        bb.put(messageEncoded);
        return bb;
    }

    public static Message decodeBroadcastMessage(ByteBuffer bb) {
        bb.flip();
        short lenOfLogin = bb.getShort();
        String login = decodeString(bb, lenOfLogin);
        int lenOfMessage = bb.getInt();
        String message = decodeString(bb, lenOfMessage);
        return new Message(login, message);
    }

    public static Message decodeBroadcastMessage(SocketChannel sc) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(Short.BYTES);
        sc.read(bb);
        bb.flip();
        short lenOfLogin = bb.getShort();
        bb = ByteBuffer.allocate(lenOfLogin);
        sc.read(bb);
        bb.flip();
        String login = decodeString(bb, lenOfLogin);
        bb = ByteBuffer.allocate(Integer.BYTES);
        sc.read(bb);
        bb.flip();
        int lenOfMessage = bb.getInt();
        bb = ByteBuffer.allocate(lenOfMessage);
        sc.read(bb);
        bb.flip();
        String message = decodeString(bb, lenOfMessage);
        return new Message(login, message);
    }

    public static ByteBuffer encodeAskPrivateConnection(String pseudo) {
        ByteBuffer encodedPseudo = UTF8.encode(pseudo);
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + encodedPseudo.remaining());
        bb.put(Query.ASK_SRV_PRIVATE_CON.getOperationCode());
        bb.putShort((short) encodedPseudo.remaining());
        bb.put(encodedPseudo);
        return bb;
    }

    public static ByteBuffer encodeAskPrivateConnectionRelayToTarget(String pseudo) {
        ByteBuffer encodedPseudo = UTF8.encode(pseudo);
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + encodedPseudo.remaining());
        bb.put(Query.ASK_SRV_PRIVATE_CON_RELAY_TARGET.getOperationCode());
        bb.putShort((short) encodedPseudo.remaining());
        bb.put(encodedPseudo);
        return bb;
    }

    public static String decodeAskPrivateConnectionRelayToTarget(SocketChannel sc) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(Short.BYTES);
        sc.read(bb);
        bb.flip();
        short lenLogin = bb.getShort();
        bb = ByteBuffer.allocate(lenLogin);
        sc.read(bb);
        bb.flip();
        return decodeString(bb, lenLogin);
    }


    public static ByteBuffer encodeResponseToServerPrivateConnRefused() {
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES * 2);
        bb.put(Query.RESP_SRV_PRIVATE_CON.getOperationCode());
        bb.put(Byte.MIN_VALUE);
        return bb;
    }

    public static ByteBuffer encodeResponseToServerPrivateConnAccepted(InetAddress address, String pseudo, int port, long secureNumber) {
        byte[] addressAsByteArray = address.getAddress();
        ByteBuffer encodedPseudo = UTF8.encode(pseudo);
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES * 3 + addressAsByteArray.length + Integer.BYTES + Long.BYTES + Short.BYTES + encodedPseudo.remaining());
        bb.put(Query.RESP_SRV_PRIVATE_CON.getOperationCode());
        bb.put(Byte.MAX_VALUE);
        bb.put((byte) addressAsByteArray.length);
        bb.put(addressAsByteArray);
        bb.putInt(port);
        bb.putLong(secureNumber);
        bb.putShort((short) encodedPseudo.remaining());
        bb.put(encodedPseudo);
        return bb;
    }

    public static ByteBuffer encodeRelayResponseToServerPrivateConnAccepted(byte[] address, int port, long secureNumber, String pseudo) {
        ByteBuffer encode = UTF8.encode(pseudo);
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES * 3 + address.length + Integer.BYTES + Long.BYTES + Short.BYTES + encode.remaining());
        bb.put(Query.RESP_SRC_PRIVATE_CON_RELAY_CLIENT.getOperationCode());
        bb.put(Byte.MAX_VALUE);
        bb.put((byte) address.length);
        bb.put(address);
        bb.putInt(port);
        bb.putLong(secureNumber);
        bb.putShort((short) encode.remaining());
        bb.put(encode);
        return bb;
    }

    public static PrivateConnResponse decodeRelayResponseToServerPrivateConnAccepted(ByteBuffer bb) throws UnknownHostException {
        bb.flip();
        if (!(bb.get() == Byte.MAX_VALUE)) {
            return new PrivateConnResponse();
        }
        byte lenOfIp = bb.get();
        byte[] ipAddress = new byte[lenOfIp];
        for (int i = 0; i < lenOfIp; i++) {
            ipAddress[i] = bb.get();
        }
        int port = bb.getInt();
        long secureNumber = bb.getLong();
        short lenOfPseudo = bb.getShort();
        String pseudo = decodeString(bb, lenOfPseudo);
        return new PrivateConnResponse(pseudo, InetAddress.getByAddress(ipAddress), port, secureNumber);
    }

    public static class PrivateConnResponse {
        private final boolean accept;
        private final InetAddress address;
        private final int port;
        private final long secureNumber;
        private final String pseudo;

        PrivateConnResponse() {
            this(null, null, 0, 0);
        }

        PrivateConnResponse(String pseudo, InetAddress address, int port, long secureNumber) {
            this.accept = true;
            this.pseudo = Objects.requireNonNull(pseudo);
            this.address = Objects.requireNonNull(address);
            this.port = port;
            this.secureNumber = secureNumber;
        }

        public boolean isAccept() {
            return accept;
        }

        public InetAddress getAddress() {
            return address;
        }

        public OptionalInt getPort() {
            return port != 0 ? OptionalInt.of(port) : OptionalInt.empty();
        }

        public OptionalLong getSecureNumber() {
            return secureNumber != 0 ? OptionalLong.of(secureNumber) : OptionalLong.empty();
        }

        public String getPseudo() {
            return pseudo;
        }
    }

    public static ByteBuffer encodeFirstConnectionRequest(long secureNumber) {
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + Long.BYTES);
        bb.put(Query.FIRST_ACCEPT_ON_CLIENT.getOperationCode());
        bb.putLong(secureNumber);
        return bb;
    }

    public static ByteBuffer encodeClientAccepted() {
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES);
        bb.put(Query.ACCEPT_CLIENT.getOperationCode());
        return bb;
    }

    public static ByteBuffer encodeClientRefused() {
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES);
        bb.put(Query.REFUSE_CLIENT.getOperationCode());
        return bb;
    }

    public static boolean isDirectMessageAccepted(ByteBuffer bb) {
        bb.flip();
        byte value = bb.get();
        byte expected = Query.ACCEPT_CLIENT.getOperationCode();
        return value == expected;
    }

    public static ByteBuffer encodeMessageDirectClient(String message) {
        ByteBuffer encode = UTF8.encode(message);
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + encode.remaining());
        bb.put(Query.MESSAGE_DIRECT_CLIENT.getOperationCode());
        bb.putInt(encode.remaining());
        bb.put(encode);
        return bb;
    }

    public static String decodeMessageDirectClient(ByteBuffer bb) {
        bb.flip();
        int sizeMessage = bb.getInt();
        return decodeString(bb, sizeMessage);
    }

    public static long decodeFirstConnectionRequest(ByteBuffer bb) {
        bb.flip();
        bb.get();
        return bb.getLong();
    }

    private static String decodeString(ByteBuffer bb, int size) {
        ByteBuffer loginBb = ByteBuffer.allocate(size);
        for (int i = 0; i < size; i++) {
            loginBb.put(bb.get());
        }
        loginBb.flip();
        return UTF8.decode(loginBb).toString();
    }

    public static ByteBuffer encodeFileRequest(String fileName) {
        ByteBuffer encode = UTF8.encode(fileName);
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + encode.remaining());
        bb.put(Query.CLIENT_FILE_REQUEST.getOperationCode());
        bb.putShort((short) encode.remaining());
        bb.put(encode);
        return bb;
    }

    public static String decodeFileRequest(ByteBuffer bb) {
        bb.flip();
        short size = bb.getShort();
        return decodeString(bb, size);
    }

    public static ByteBuffer encodeRefuseFileRequestResponse() {
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES * 2);
        bb.put(Query.CLIENT_FILE_REQUEST_RESPONSE.getOperationCode());
        bb.put(Byte.MIN_VALUE);
        return bb;
    }

    public static ByteBuffer encodeAcceptFileRequestResponse(int port, long sessionCode) {
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES * 2 + Integer.BYTES + Long.BYTES);
        bb.put(Query.CLIENT_FILE_REQUEST_RESPONSE.getOperationCode());
        bb.put(Byte.MAX_VALUE);
        bb.putInt(port);
        bb.putLong(sessionCode);
        return bb;
    }

    public static ResponseFileTransfer decodeFileRequestResponse(ByteBuffer bb) {
        bb.flip();
        byte b = bb.get();
        if (b == Byte.MIN_VALUE) {
            return new ResponseFileTransfer();
        }
        int port = bb.getInt();
        long secureCode = bb.getLong();
        return new ResponseFileTransfer(port, secureCode);
    }

    public static class ResponseFileTransfer {
        private final boolean accept;
        private final int port;
        private final long sessionCode;

        private ResponseFileTransfer() {
            this.accept = false;
            this.port = 0;
            this.sessionCode = 0;
        }

        private ResponseFileTransfer(int port, long sessionCode) {
            this.accept = true;
            this.port = port;
            this.sessionCode = sessionCode;
        }

        public boolean isAccept() {
            return accept;
        }

        public int getPort() {
            return port;
        }

        public long getSessionCode() {
            return sessionCode;
        }
    }

    public static ByteBuffer encodeFileTransfer(long secureNumber, byte[] bytes) {
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES * 2 + bytes.length);
        bb.putLong(secureNumber);
        bb.putLong(bytes.length);
        bb.put(bytes);
        return bb;
    }

    public static FileTransfer decodeFileTransfer(SocketChannel client) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES * 2);
        client.read(bb);
        bb.flip();
        long secureNumber = bb.getLong();
        long size = bb.getLong();
        ByteBuffer bbData = ByteBuffer.allocate((int) size);
        client.read(bbData);
        bbData.flip();
        byte[] array = bbData.array();
        return new FileTransfer(array, secureNumber);
    }

    public static class FileTransfer {
        private final long sessionCode;
        private final byte[] data;

        private FileTransfer(byte[] data, long sessionCode) {
            this.data = data;
            this.sessionCode = sessionCode;
        }

        public byte[] getData() {
            return data;
        }

        public long getSessionCode() {
            return sessionCode;
        }
    }
}