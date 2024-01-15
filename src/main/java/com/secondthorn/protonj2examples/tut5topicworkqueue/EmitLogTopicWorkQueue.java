package com.secondthorn.protonj2examples.tut5topicworkqueue;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Sender;
import org.apache.qpid.protonj2.client.SenderOptions;
import org.apache.qpid.protonj2.client.Tracker;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

import java.util.Arrays;

public class EmitLogTopicWorkQueue {
    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            SenderOptions senderOptions = new SenderOptions();
            senderOptions.targetOptions().capabilities("topic");
            String exchange = "topic_logs";
            // routing key should be app1 or app2 for this example
            String routingKey = args.length > 0 ? args[0] : "app1";
            String messagePayload = "Hello World!";
            if (args.length > 1) {
                messagePayload = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            }
            Message<String> message = Message.create(messagePayload)
                    .property("routingKey", routingKey)
                    .durable(true);
            Sender sender = connection.openSender(exchange + "." + routingKey, senderOptions);
            Tracker tracker = sender.send(message);
            tracker.awaitSettlement();
            System.out.println("Sent message to " + sender.address() + ": " + message.body());
        } catch (ClientException ex) {
            ex.printStackTrace();
        }
    }
}
