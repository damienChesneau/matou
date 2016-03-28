package fr.upem.matou.common;

/**
 * @author Damien Chesneau
 */
public enum Query {
    CONNECT_SERVER((byte) 1),
    VALIDATE_CONNECTION((byte) 2),
    ERROR_CONNECTION((byte) -1),
    SEND_SRV_MESSAGE((byte) 3),
    BROADCAST_CLIENTS_MESSAGE((byte) 4),
    ASK_SRV_PRIVATE_CON((byte) 5),
    ERROR_PSEUDO_UNKNOWN((byte) -2),
    ASK_SRV_PRIVATE_CON_RELAY_TARGET((byte) 6),
    RESP_SRV_PRIVATE_CON((byte) 7),
    RESP_SRC_PRIVATE_CON_RELAY_CLIENT((byte) 8),
    FIRST_ACCEPT_ON_CLIENT((byte) 21),
    ACCEPT_CLIENT((byte) 10),
    REFUSE_CLIENT((byte) -3),
    MESSAGE_DIRECT_CLIENT((byte) 22),
    CLIENT_FILE_REQUEST((byte) 23),
    CLIENT_FILE_REQUEST_RESPONSE((byte) 24);
    private final byte operationCode;

    Query(byte operationCode) {
        this.operationCode = operationCode;
    }

    public byte getOperationCode() {
        return operationCode;
    }

}