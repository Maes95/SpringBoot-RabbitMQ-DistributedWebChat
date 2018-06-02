package com.chat;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

public class Chat extends ChatConsumer{

    private static final String EXCHANGE_NAME = "MANAGER";

    private final String chatName;
    private final HashMap<String, User> users;
    private final ConnectionFactory factory;

    public Chat(String name){
        this.chatName = name;
        this.users = new HashMap<>();
        this.factory = new ConnectionFactory();
        this.factory.setHost("localhost");
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, EXCHANGE_NAME, this.chatName);
            channel.basicConsume(queue, true, this);
            System.out.println("CREATE CONNECTION "+chatName);
        } catch (IOException e) {
            System.err.println("Can't create chat room: IOException");
        } catch (TimeoutException e){
            System.err.println("Can't create chat room: Timeout");
        }
    }

    public void subscribe(User user){
        users.put(user.getName(), user);
    }

    public void unSubscribe(User user){
        users.remove(user.getName());
    }

    public boolean exist(String name){
        return users.containsKey(name);
    }

    public boolean isEmpty(){
        return users.isEmpty();
    }

    @Override
    public void publish(String message){
        try {
            this.channel.basicPublish(EXCHANGE_NAME, this.chatName, null, message.getBytes());
        } catch (IOException e) {
            System.out.println("Can't publish message");
        }
    }

    @Override
    public void handleQueueMessage(String message) {
        for(User user : this.users.values()) user.send(message);
    }

    public void destroy(){
        try{
            this.connection.close();
            System.out.println("CHANNEL CLOSED");
        }catch (AlreadyClosedException e){
            System.err.println("Channel already closed");
        }catch (IOException e){
            System.err.println(e.getCause());
        }
    }


}
