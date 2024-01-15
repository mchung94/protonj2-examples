package com.secondthorn.protonj2examples.tut5;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Delivery;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Receiver;
import org.apache.qpid.protonj2.client.ReceiverOptions;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

public class ReceiveLogsTopic {
    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            String exchange = "topic_logs";
            ReceiverOptions receiverOptions = new ReceiverOptions();
            receiverOptions.sourceOptions().capabilities("topic");
            String routingKey = args.length > 0 ? args[0] : "#";
            Receiver receiver = connection.openReceiver(exchange + "." + routingKey, receiverOptions);
            while (true) {
                Delivery delivery = receiver.receive();
                Message<String> message = delivery.message();
                // print all annotations and properties set by the sender
                message.forEachAnnotation((key, value) -> System.out.println("Annotation: " + key + " " + value));
                message.forEachProperty((key, value) -> System.out.println("Property: " + key + " " + value));
                // When you openReceiver with "topic_logs.#", receiver.address()
                // is going to be "topic_logs.#", not the sender's address of
                // "topic_logs.kern.critical" for example. So I can send these
                // as properties for the receiver to get the sender's address
                System.out.println("Received message from address " + receiver.address() +
                        " and routingKey = " + message.property("routingKey") +
                        " with body: " + message.body());
            }
        } catch (ClientException ex) {
            ex.printStackTrace();
        }
    }
}
