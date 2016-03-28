package fr.upem.matou.server;

import fr.upem.matou.common.Message;
import fr.upem.matou.common.Querys;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author Damien Chesneau
 */
public class QuerysTest {

    @Test
    public void testServerConnect() {
        ByteBuffer bb = Querys.encodeServerConnect("damien");
        byte b = Querys.decodeOperationCode(bb);
        Assert.assertEquals(1, b);
        String login = Querys.decodeServerConnect(bb);
        Assert.assertEquals("damien", login);
    }

    @Test
    public void testSendMessageToServer() {
        String expectedMessage = "Hello, it's me the wonderful message :)";
        ByteBuffer bb = Querys.encodeSendMessageToServer(expectedMessage);
        byte b = Querys.decodeOperationCode(bb);
        Assert.assertEquals(3, b);
        String message = Querys.decodeSendMessageToServer(bb);
        Assert.assertEquals(expectedMessage, message);
    }

    @Test
    public void testBroadcastMessage() {
        String expectedMessage = "Hello, it's me the wonderful message :)";
        String expectedLogin = "Damien";
        ByteBuffer bb = Querys.encodeBroadcastMessage(expectedLogin, expectedMessage);
        byte b = Querys.decodeOperationCode(bb);
        Assert.assertEquals(4, b);
        Message message = Querys.decodeBroadcastMessage(bb);
        Assert.assertEquals(expectedLogin, message.getLogin());
        Assert.assertEquals(expectedMessage, message.getMessage());
    }

}
