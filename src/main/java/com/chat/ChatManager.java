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

    private static ChatManager ourInstance = new ChatManager();
    public static ChatManager getInstance() {
        return ourInstance;
    }

    private Map<String, Chat> rooms;

    private ChatManager() {
        this.rooms = new ConcurrentHashMap<>();
    }

    public Publisher subscribe(User user){
        Chat chat = getChat(user.getChat());
        chat.subscribe(user);
        return chat;
    }

    public void unSubscribe(User user){
        String chatName = user.getChat();
        Chat room = rooms.get(chatName);
        System.out.println("SIZE: "+rooms.size());
        System.out.println("AQUI: "+chatName+" "+room);

        if(room != null){
            room.unSubscribe(user);
            if(room.isEmpty()){
                // Remove chat room if it's empty
                System.out.println("DELETE: "+chatName);
                rooms.remove(chatName);
                room.destroy();
            }
        }
    }

    public boolean userExist(String user_name){
        return rooms.values().stream().anyMatch((chat) ->
                chat.exist(user_name)
        );
    }

    private synchronized Chat getChat(String chatName){
        if(!rooms.containsKey(chatName)){
            this.rooms.put(chatName, new Chat(chatName));
        }
        return rooms.get(chatName);
    }
}
