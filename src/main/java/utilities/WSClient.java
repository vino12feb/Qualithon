package utilities;

import java.net.URI;
import java.net.URISyntaxException;

public class WSClient {
    String responseMessage = "";

    public String connectWSClient(String uri, String requestMessage) {
        try {
            // open websocket
            final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI(uri));

            // add listener
            clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
                public void handleMessage(String message) {
                    responseMessage = message;
                }
            });

            // send message to websocket
            clientEndPoint.sendMessage(requestMessage);

            // wait 5 seconds for messages from websocket
            Thread.sleep(5000);

        } catch (InterruptedException ex) {
            System.err.println("InterruptedException exception: " + ex.getMessage());
        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        }

        return responseMessage;
    }

}
