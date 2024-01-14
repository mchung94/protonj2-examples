package com.secondthorn.protonj2examples.tut1;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Sender;
import org.apache.qpid.protonj2.client.SenderOptions;
import org.apache.qpid.protonj2.client.Tracker;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

public class Send {
    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            SenderOptions senderOptions = new SenderOptions();
            // the queue capability does what we want here, create the q-hello
            // queue bound to the q-hello address, and use anycast routing type
            senderOptions.targetOptions().capabilities("queue");
            Sender sender = connection.openSender("q-hello", senderOptions);
            Message<String> message = Message.create("Hello World!");
            Tracker tracker = sender.send(message);
            tracker.awaitSettlement();
            System.out.println("Sent message to " + sender.address() + ": " + message.body());
        } catch (ClientException ex) {
            ex.printStackTrace();
        }
    }
}
