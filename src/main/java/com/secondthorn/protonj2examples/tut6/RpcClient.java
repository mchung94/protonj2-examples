package com.secondthorn.protonj2examples.tut6;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Delivery;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Receiver;
import org.apache.qpid.protonj2.client.Sender;
import org.apache.qpid.protonj2.client.SenderOptions;
import org.apache.qpid.protonj2.client.Tracker;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

import java.util.UUID;

public class RpcClient {
    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616);
             Receiver dynamicReceiver = connection.openDynamicReceiver()) {
            SenderOptions senderOptions = new SenderOptions();
            senderOptions.targetOptions().capabilities("queue");
            Sender sender = connection.openSender("rpc_queue", senderOptions);

            Integer argument = args.length > 0 ? Integer.valueOf(args[0]) : 30;
            String correlationId = UUID.randomUUID().toString();
            Message<Integer> message = Message.create(argument)
                    .correlationId(correlationId)
                    .replyTo(dynamicReceiver.address());
            message.durable(true); // make each message persistent
            Tracker tracker = sender.send(message);
            tracker.awaitSettlement();
            System.out.println("Sent message to " + sender.address() + ": " + message.body());

            while (true) {
                Delivery response = dynamicReceiver.receive();
                Message<Integer> responseMessage = response.message();
                if (responseMessage.correlationId().equals(correlationId)) {
                    System.out.println("Got response " + responseMessage.body());
                }
                response.accept();
            }
        } catch (ClientException ex) {
            ex.printStackTrace();
        }
    }
}
