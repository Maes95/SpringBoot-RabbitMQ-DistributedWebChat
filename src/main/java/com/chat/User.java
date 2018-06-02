package com.chat;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.json.JSONObject;

@ServerEndpoint("/chat")
public class User{

    private static String EXCHANGE_NAME = "MANAGER";
    private static final String DUPLICATE_MSG = "{\"type\":\"system\",\"message\":\"Ya existe un usuario con ese nombre\"}";

    private Session session;
    private String name;
    private String chat;
    private ChatManager manager;
    private Publisher publisher;

    @OnOpen
    public void open(Session session){
        this.session = session;
        this.manager = ChatManager.getInstance();
    }

    @OnMessage
    public void handleMessage(Session session, String message){

        if(this.name != null){
            // Broadcast message
            if(this.publisher != null) this.publisher.publish(message);
        }else{
            // Init message
            newUser(message);
        }

    }

    @OnClose
    public void close(Session session) throws IOException, TimeoutException {

        if(this.name != null){
            manager.unSubscribe(this);
        }

    }

    @OnError
    public void onError(Session session, Throwable thr) {
        System.err.println("Client "+session.getId()+" error: "+thr.getMessage());
    }

    public void send(String message){
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            //System.err.println("Can't send message to user");
        }
    }

    private void newUser(String message){

        JSONObject jsonMessage = new JSONObject(message);

        this.chat = jsonMessage.getString("chat");
        this.name = jsonMessage.getString("name");

        if( manager.userExist(name) ){
            send(DUPLICATE_MSG);
        }else{
            this.publisher = this.manager.subscribe(this);
        }
    }

    public String getName(){
        return this.name;
    }

    public String getChat(){
        return this.chat;
    }

}
