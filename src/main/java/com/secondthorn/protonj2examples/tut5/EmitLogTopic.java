package com.secondthorn.protonj2examples.tut5;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Sender;
import org.apache.qpid.protonj2.client.SenderOptions;
import org.apache.qpid.protonj2.client.Tracker;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

import java.util.Arrays;
import java.util.Map;

public class EmitLogTopic {
    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            SenderOptions senderOptions = new SenderOptions();
            senderOptions.targetOptions().capabilities("topic");
            // In the RabbitMQ example, exchange = "topic_logs" and the routing
            // key = <facility>.<severity> e.g. "anonymous.info"
            // Let's use address = <exchange>.<facility>.<severity>
            String exchange = "topic_logs";
            String routingKey = args.length > 0 ? args[0] : "anonymous.info";
            String messagePayload = "Hello World!";
            if (args.length > 1) {
                messagePayload = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            }
            // Set a routingKey property that the client can read, because even
            // though the sender's address might be "topic_logs.kern.critical",
            // the receiver might use the address "topic_logs.kern.*", so the
            // receiver doesn't really know what the original address was.
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
