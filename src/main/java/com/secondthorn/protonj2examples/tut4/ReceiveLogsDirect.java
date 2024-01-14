package com.secondthorn.protonj2examples.tut4;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Delivery;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Receiver;
import org.apache.qpid.protonj2.client.ReceiverOptions;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

public class ReceiveLogsDirect {
    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            ReceiverOptions receiverOptions = new ReceiverOptions();
            receiverOptions.sourceOptions().capabilities("topic");
            String severity = args.length > 0 ? args[0] : "info";
            Receiver receiver = connection.openReceiver("direct_logs." + severity, receiverOptions);
            // This is different from RabbitMQ's tutorial because we're creating
            // a queue with only one binding, instead of having multiple bindings
            // on the same queue. In other words, I don't know how to have the
            // receiver listen to both direct_logs.warning and
            // direct_logs.error. There may be a workaround using diverts, but
            // I have to investigate that.
            while (true) {
                Delivery delivery = receiver.receive();
                Message<String> message = delivery.message();
                System.out.println("Received message from address " + receiver.address() + " with body: " + message.body());
            }
        } catch (ClientException ex) {
            ex.printStackTrace();
        }
    }
}
