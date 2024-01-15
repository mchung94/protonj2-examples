# protonj2-examples
An adaptation of RabbitMQ tutorial examples using ActiveMQ Artemis and Qpid
ProtonJ2.

This is just a work in progress while I'm learning. It's not RabbitMQ or AMQP
0-9-1, so the concepts are different, and I'm still trying to understand Qpid
ProtonJ2 using the API docs and trial-and-error.

# Running Artemis
I'm using docker compose to run Artemis 2.31.2 with anonymous login turned on.
There is a `docker-compose.yml` in the top-level directory, so I can just run
commands like:
```
docker compose up -d
docker compose down
docker compose logs -f
docker compose restart
```
Once it's running, go to http://localhost:8161 and login using the default
login artemis/artemis. More details are at
https://activemq.apache.org/components/artemis/documentation/latest/docker.html

# Running the Examples
I'm using Java 17 and the maven wrapper (`./mvnw` or `mvnw.cmd`)
This is an ordinary Maven project, so you can run `./mvnw compile` for example.

Also, I've added the `exec-maven-plugin` so you can run each class that has a
main method in it.

For example, to run tutorial 4:
```
# Running the receiver with command line args:
./mvnw compile exec:java -Dexec.mainClass="com.secondthorn.protonj2examples.tut4.ReceiveLogsDirect" -Dexec.args="error"

# Running the sender with command line args:
./mvnw compile exec:java -Dexec.mainClass="com.secondthorn.protonj2examples.tut4.EmitLogDirect" -Dexec.args="error This is a log message."
```

# Tutorial Notes
Keep in mind that I'm just starting to learn this, so I might misunderstand the
concepts and capabilities of Artemis and the Qpid ProtonJ2 library.

## Tutorial 1: Hello world!
In RabbitMQ, you can send to a specific queue using the default exchange (empty
string) and routing_key set to the name of the queue.

In Artemis, the closest thing to an exchange is an address. So far, it doesn't
sound like the routing key concept exists, but you can use addresses to support
the functionality.

There is also the concept of a fully-qualified queue name, which is the address
and queue named separated by two colons `::` like my_address::my_queue. This is
for sending/receiving messages on a specific queue.

There are two routing types:
- anycast: send to one queue on the address
- multicast: send to every queue on the address

The Artemis documentation says for most protocols that support this pattern,
it is customary to set both the address and queue to the name of the queue and
use anycast. In the code, if I set the capabilities to "queue", it'll create
the queue and address with the right parameters for this example.

## Tutorial 2: Work Queues
On the receiver (Worker.java):
- I've disabled auto-ack on the receiver with `autoAccept(false)`
- I manually accept the message after processing: `delivery.accept()`
- I set the prefetch to 1 using `creditWindow(1)`. This helps in cases like
  when I publish 10 messages, then start up the worker process. Without this,
  one worker might process all the messages while the other just waits with
  no work to do because the other worker prefetched them all.

On the sender (NewTask.java):
- I've added a second parameter, an integer to be able to send the same
  message multiple times, for convenience.
- The queue is already durable from setting the capabilities, but I also add
  `message.durable(true)` to the message so that it is persistent.

## Tutorial 3: Publish/Subscribe
I set the capabilities to "topic" for the following behavior:
- fanout - all consumers receive all messages
- temporary queues - create a queue with an auto-generated name, delete the
  queue when the worker closes.

## Tutorial 4: Routing
In the RabbitMQ tutorial, direct exchanges route messages only to the queues
where the routing key exactly matches. There's an exchange called "direct_logs"
and messages are sent with a routing key, a logging severity like "info",
"warning", or "error". The idea is that different consumers can listen to the
direct_logs exchange but only receive messages of the severity they are
interested in.

The way I handled it in my example is to simulate the exchange + routing key
concept with addresses named `<exchange>.<routing_key>` and then have queues
bound to that address. For example, `direct_logs.info`, `direct_logs.warning`,
and `direct_logs.error`.

You can have multiple queues bound to the same address, so error logs can go to
multiple consumers. But I can't find a good way to have a single queue listen
to multiple addresses - in other words, how to have a single receiver listen
for both error and warning logs.

You may be able to set up queues to listen to multiple addresses from the
broker config file? But I have not found anything about doing it either from
the Artemis web console interface, or programmatically.

## Tutorial 5: Topics
At this time, I learned about setting properties on a Message - they're just
a String to Object mapping.

If the sender sends a message to the address "topic_logs.kern.critical", a
receiver can receive the message if they open the address "topic_logs.#", but
then there's no direct way I know of to know that the original address of the
message was "topic_logs.kern.critical".  But if I set that as a property of
the message, the receiver can look it up.

### Tutorial 5 Part 2: Topic Work Queues
This is something I was personally curious about, even though it's not on the
RabbitMQ tutorial. I wanted to combine Topic and Work Queue functionality.

I want an example where a message producer sends messages to addresses like:
- topic_logs.app1
- topic_logs.app2

Then I could create the work queue pattern on each address.
- TopicLogsApp1Receiver creates a work queue that listens to topic_logs.app1
- TopicLogsApp2Receiver creates a work queue that listens to topic_logs.app2
- TopicLogsAllReceiver creates a work queue that listens to topic_logs.#

These will be named, durable queues where all messages are persistent. Then I
can run many instances of each receiver, and they can split the work like a work
queue.