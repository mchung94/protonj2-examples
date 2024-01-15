package com.secondthorn.protonj2examples.tut5topicworkqueue;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Delivery;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Receiver;
import org.apache.qpid.protonj2.client.ReceiverOptions;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

public class TopicLogsAllReceiver {
    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            ReceiverOptions receiverOptions = new ReceiverOptions();
            receiverOptions.autoAccept(false); // manually ack / accept deliveries
            receiverOptions.creditWindow(1); // prefetch 1
            receiverOptions.sourceOptions().capabilities("queue");
            Receiver receiver = connection.openReceiver("topic_logs.#::topic_logs_all", receiverOptions);
            while (true) {
                Delivery delivery = receiver.receive();
                Message<String> message = delivery.message();
                System.out.println("Received message from address " + receiver.address() +
                        " with routing key " + message.property("routingKey") +
                        " and body " + message.body());
                // sleep to pretend I'm doing work, and multiple instances of
                // this process can pick up different messages
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                delivery.accept();
            }
        } catch (ClientException ex) {
            ex.printStackTrace();
        }
    }
}
