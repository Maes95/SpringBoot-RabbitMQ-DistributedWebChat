package com.chat;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeoutException;

public class ChatManager {

    private static String EXCHANGE_NAME = "MANAGER";
    private static ChatManager ourInstance = new ChatManager();
    public static ChatManager getInstance() {
        return ourInstance;
    }

    private Map<String, Set<String>> rooms;
    private ConnectionFactory factory;

    private ChatManager() {
        this.rooms = new ConcurrentHashMap<>();
        this.factory = new ConnectionFactory();
        this.factory.setHost("localhost");
    }

    private void addUser(String name, String chat){
        if(!rooms.containsKey(chat)){
            // Chat doesn't exist
            rooms.put(chat, new ConcurrentSkipListSet());
        }
        rooms.get(chat).add(name);
    }

    public Channel subscribe(String name, String chat, UserConsumer user){
        Channel channel = null;
        Connection connection = null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, EXCHANGE_NAME, chat);
            channel.basicConsume(queue, true, user);
        } catch (IOException e) {
            System.err.println("Can't subscribe user to chat: IOException");
        } catch (TimeoutException e){
            System.err.println("Can't subscribe user to chat: Timeout");
        } finally {
            if(channel != null && connection != null){
                user.setConection(connection, channel);
                addUser(name, chat);
            }
        }
        return channel;
    }

    public void deleteUser(String name, String chat){
        Set<String> room = rooms.get(chat);
        room.remove(name);
        if(room.isEmpty()){
            // Remove chat room if it's empty
            rooms.remove(chat);
        }
    }

    public boolean userExist(String user_name){
        return rooms.values().stream().anyMatch((chat) ->
                chat.contains(user_name)
        );
    }
}
