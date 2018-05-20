package com.chat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.*;


import org.json.JSONObject;

@ServerEndpoint("/chat")
public class ChatManager {

    private static String EXCHANGE_NAME = "MANAGER";
    private static final Map<String, Map<String, User>> rooms = new ConcurrentHashMap<>();
    private static final String DUPLICATE_MSG = "{\"type\":\"system\",\"message\":\"Ya existe un usuario con ese nombre\"}";

    private User user;

    @OnOpen
    public void open(Session session){}

    @OnMessage
    public void handleMessage(Session session, String message) throws Exception {

        if(this.user != null){
            // Broadcast message
            user.broadcast(message);
        }else{
            newUser(session,message);
        }

    }

    @OnClose
    public void close(Session session){

        if(this.user != null){
            Map<String, User> chat = rooms.get(this.user.getChat());
            chat.remove(this.user.getName());
            if(chat.isEmpty()){
                // Remove chat room if it's empty
                rooms.remove(this.user.getChat());
            }
            user.disconnect();
        }

    }

    @OnError
    public void onError(Session session, Throwable thr) {

        System.out.println("Cliente "+session.getId()+" desconectado");
        thr.getCause().printStackTrace();

    }

    private void newUser(Session session, String message) throws Exception{

        JSONObject jsonMessage = new JSONObject(message);

        String chat = jsonMessage.getString("chat");
        String name = jsonMessage.getString("name");

        if( this.userExist(name) ){
            this.user.send(DUPLICATE_MSG);
        }else{
            if(!rooms.containsKey(chat)){
                // Chat doesn't exist
                rooms.put(chat, new ConcurrentHashMap<>());
            }
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            this.user = new User(name, chat, session, channel, EXCHANGE_NAME);
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, chat);
            channel.basicConsume(queueName, true, this.user);
            rooms.get(chat).put(name, this.user);
        }

    }

    private boolean userExist(String user_name){
        return rooms.values().stream().anyMatch((chat) ->
            chat.containsKey(user_name)
        );
    }

}
