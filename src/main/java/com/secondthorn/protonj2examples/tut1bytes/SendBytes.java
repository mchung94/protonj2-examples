package com.secondthorn.protonj2examples.tut1bytes;

import org.apache.qpid.protonj2.client.Client;
import org.apache.qpid.protonj2.client.Connection;
import org.apache.qpid.protonj2.client.Message;
import org.apache.qpid.protonj2.client.Sender;
import org.apache.qpid.protonj2.client.SenderOptions;
import org.apache.qpid.protonj2.client.Tracker;
import org.apache.qpid.protonj2.client.exceptions.ClientException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SendBytes {
    public static byte[] convertBookToBytes(Book book) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(book);
            oos.flush();
            return bos.toByteArray();
        }
    }

    public static void main(String[] args) {
        try (Client client = Client.create();
             Connection connection = client.connect("localhost", 61616)) {
            SenderOptions senderOptions = new SenderOptions();
            Book book = new Book("A Boring Book", "John Doe", 1997);
            // the queue capability does what we want here, create the q-hello
            // queue bound to the q-hello address, and use anycast routing type
            senderOptions.targetOptions().capabilities("queue");
            Sender sender = connection.openSender("q-book", senderOptions);
            byte[] bookBytes = convertBookToBytes(book);
            Message<byte[]> message = Message.create(bookBytes);
            Tracker tracker = sender.send(message);
            tracker.awaitSettlement();
            System.out.println("Sent message to " + sender.address() + ", body length " + message.body().length);
        } catch (ClientException | IOException ex) {
            ex.printStackTrace();
        }
    }
}
