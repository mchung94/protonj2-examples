package com.secondthorn.protonj2examples.tut3;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Sender;
import org.apache.qpid.protonj2.client.SenderOptions;
import org.apache.qpid.protonj2.client.Tracker;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

public class EmitLog {
    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            SenderOptions senderOptions = new SenderOptions();
            senderOptions.targetOptions().capabilities("topic");
            Sender sender = connection.openSender("logs", senderOptions);
            String messagePayload = args.length > 0 ? args[0] : "Hello World!";
            int messageCount = args.length > 1 ? Integer.parseInt(args[1]) : 1;
            Message<String> message = Message.create(messagePayload);
            message.durable(true); // make each message persistent
            for (int i=0; i<messageCount; i++) {
                Tracker tracker = sender.send(message);
                tracker.awaitSettlement();
            }
            System.out.println("Sent message to " + sender.address() + ": " + message.body());
        } catch (ClientException ex) {
            ex.printStackTrace();
        }
    }
}
