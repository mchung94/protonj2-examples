package com.secondthorn.protonj2examples.tut1bytes;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Delivery;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Receiver;
import org.apache.qpid.protonj2.client.ReceiverOptions;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ReceiveBytes {
    public static Book convertBytesToBook(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            Book book = (Book)ois.readObject();
            return book;
        }
    }

    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            ReceiverOptions receiverOptions = new ReceiverOptions();
            receiverOptions.sourceOptions().capabilities("queue");
            Receiver receiver = connection.openReceiver("q-book", receiverOptions);
            while (true) {
                Delivery delivery = receiver.receive();
                Message<byte[]> message = delivery.message();
                Book book = convertBytesToBook(message.body());
                System.out.println("Received book with author: " + book.getAuthor() +
                        " title: " + book.getTitle() +
                        " year: " + book.getYear());
            }
        } catch (ClientException | IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
