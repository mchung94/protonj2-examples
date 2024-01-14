package com.secondthorn.protonj2examples.tut2;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Sender;
import org.apache.qpid.protonj2.client.SenderOptions;
import org.apache.qpid.protonj2.client.Tracker;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

public class NewTask {
    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            SenderOptions senderOptions = new SenderOptions();
            // I can't find documentation on "queue" capability, but it looks
            // like it'll make the queue and address both the same name, as is
            // customary, durable = True
            senderOptions.targetOptions().capabilities("queue");
            Sender sender = connection.openSender("q-task_queue", senderOptions);
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
