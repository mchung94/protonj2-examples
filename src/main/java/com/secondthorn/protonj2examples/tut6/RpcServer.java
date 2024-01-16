package com.secondthorn.protonj2examples.tut6;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Delivery;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Receiver;
import org.apache.qpid.protonj2.client.ReceiverOptions;
import org.apache.qpid.protonj2.client.Sender;
import org.apache.qpid.protonj2.client.Tracker;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

public class RpcServer {
    public static int fib(int n) {
        if (n == 0) {
            return 0;
        } else if (n == 1) {
            return 1;
        } else {
            return fib(n - 1) + fib(n - 2);
        }
    }

    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            ReceiverOptions receiverOptions = new ReceiverOptions();
            receiverOptions.sourceOptions().capabilities("queue");
            receiverOptions.autoAccept(false); // manually ack / accept deliveries
            receiverOptions.creditWindow(1); // prefetch 1
            Receiver receiver = connection.openReceiver("rpc_queue", receiverOptions);
            while (true) {
                Delivery delivery = receiver.receive();
                Message<Integer> message = delivery.message();
                System.out.println("Received message with body: " + message.body());
                Message<Integer> response = Message.create(fib(message.body()))
                        .correlationId(message.correlationId());
                String replyAddress = message.replyTo();
                System.out.println("Replying to " + replyAddress);
                if (replyAddress != null) {
                    Sender sender = connection.openSender(replyAddress);
                    Tracker tracker = sender.send(response);
                    System.out.println("Sent message to " + replyAddress + " with response " + response.body());
                    tracker.awaitSettlement();
                }
                delivery.accept();
            }
        } catch (ClientException ex) {
            ex.printStackTrace();
        }
    }
}
