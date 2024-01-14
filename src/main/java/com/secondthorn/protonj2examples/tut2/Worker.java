package com.secondthorn.protonj2examples.tut2;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Delivery;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Receiver;
import org.apache.qpid.protonj2.client.ReceiverOptions;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

public class Worker {
    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            ReceiverOptions receiverOptions = new ReceiverOptions();
            receiverOptions.autoAccept(false); // manually ack / accept deliveries
            receiverOptions.creditWindow(1); // prefetch 1
            // I can't find documentation on "queue" capability, but it looks
            // like it'll make the queue and address both the same name, as is
            // customary, durable = True
            receiverOptions.sourceOptions().capabilities("queue");
            Receiver receiver = connection.openReceiver("q-task_queue", receiverOptions);

            while (true) {
                Delivery delivery = receiver.receive();
                Message<String> message = delivery.message();
                System.out.println("Received message with body: " + message.body());
                try {
                    Thread.sleep(1000 * message.body().chars().filter(ch -> ch == '.').count());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Finished working on message with body: " + message.body());
                delivery.accept();
            }
        } catch (ClientException ex) {
            ex.printStackTrace();
        }
    }
}
