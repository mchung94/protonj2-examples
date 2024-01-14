package com.secondthorn.protonj2examples.tut4;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Sender;
import org.apache.qpid.protonj2.client.SenderOptions;
import org.apache.qpid.protonj2.client.Tracker;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

import java.util.Arrays;

public class EmitLogDirect {
    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            SenderOptions senderOptions = new SenderOptions();
            senderOptions.targetOptions().capabilities("topic");
            String severity = args.length > 0 ? args[0] : "info";
            String messagePayload = "Hello World!";
            if (args.length > 1) {
                messagePayload = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            }
            Message<String> message = Message.create(messagePayload);
            message.durable(true); // make each message persistent
            // There is no routing key concept, the closest thing is addresses.
            // In the RabbitMQ tutorial, the exchange = direct_logs, and the
            // routing key is the severity: info, warning, or error.
            // Here, I'm making the address the <exchange>.<routing key>
            Sender sender = connection.openSender("direct_logs." + severity, senderOptions);
            Tracker tracker = sender.send(message);
            tracker.awaitSettlement();
            System.out.println("Sent message to " + sender.address() + ": " + message.body());
        } catch (ClientException ex) {
            ex.printStackTrace();
        }
    }
}
