package com.secondthorn.protonj2examples.tut1;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Delivery;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Receiver;
import org.apache.qpid.protonj2.client.ReceiverOptions;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

public class Receive {
    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            ReceiverOptions receiverOptions = new ReceiverOptions();
            receiverOptions.sourceOptions().capabilities("queue");
            Receiver receiver = connection.openReceiver("q-hello", receiverOptions);
            while (true) {
                Delivery delivery = receiver.receive();
                Message<String> message = delivery.message();
                System.out.println("Received message with body: " + message.body());
            }
        } catch (ClientException ex) {
            ex.printStackTrace();
        }
    }
}
