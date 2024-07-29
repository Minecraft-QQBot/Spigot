package org.lonelysail.qqbot.websocket;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.lonelysail.qqbot.Utils;

import java.net.URI;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;

public class WsSender extends WebSocketClient {
    private String message = null;
    private final Utils utils;
    private final Logger logger;

    public WsSender(JavaPlugin plugin, Configuration config) {
        super(URI.create(Objects.requireNonNull(config.getString("uri"))).resolve("websocket/bot"));
        this.utils = new Utils();
        this.logger = plugin.getLogger();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("name", config.getString("name"));
        headers.put("token", config.getString("token"));
        this.setDaemon(true);
        this.addHeader("info", this.utils.encode(headers));
    }

    public boolean sendData(String event_type, HashMap<String, ?> data) {
        if (this.isClosed()) {
            this.logger.warning("无法发送数据，因为连接已关闭！正在尝试重新连接……");
            this.reconnect();
            try {
                this.wait(1000);
            } catch (InterruptedException error) {
                this.logger.warning("无法重新连接，请检查机器人运行是否正常！");
            }
            if (this.isClosed()) {
                this.logger.warning("无法重新连接，请检查机器人运行是否正常！");
                return false;
            }
        }
        HashMap<String, Object> message_data = new HashMap<>();
        message_data.put("data", data);
        message_data.put("type", event_type);
        this.send(this.utils.encode(message_data));
        while (this.message == null) {
            try {
                this.wait(100);
            } catch (InterruptedException error) {
                error.printStackTrace();
            }
        }
        HashMap<String, ?> response = this.utils.decode(this.message);
        this.message = null;
        return (boolean) response.get("success");
    }

    public void sendServerStartup() {
        HashMap<String, Object> data = new HashMap<>();
        if (this.sendData("server_startup", data)) this.logger.info("发送服务器启动消息成功！");
        else this.logger.warning("发送服务器启动消息失败！请检查机器人是否启动后再次尝试。");
    }

    public void sendServerShutdown() {
        HashMap<String, Object> data = new HashMap<>();
        if (this.sendData("server_shutdown", data)) this.logger.info("发送服务器关闭消息成功！");
        else this.logger.warning("发送服务器关闭消息失败！请检查机器人是否启动后再次尝试。");
    }

    public void sendPlayerLeft(String name) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("player", name);
        if (this.sendData("player_left", data)) this.logger.info("发送玩家离开消息成功！");
        else this.logger.warning("发送玩家离开消息失败！请检查机器人是否启动后再次尝试。");
    }

    public void sendPlayerJoined(String name) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("player", name);
        if (this.sendData("player_joined", data)) this.logger.info("发送玩家进入消息成功！");
        else this.logger.warning("发送玩家进入消息失败！请检查机器人是否启动后再次尝试。");
    }

    public void sendPlayerChat(String name, String message) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("player", name);
        data.put("message", message);
        if (this.sendData("player_chat", data)) this.logger.info("发送玩家消息成功！");
        else this.logger.warning("发送玩家消息失败！请检查机器人是否启动后再次尝试。");
    }

    public void sendPlayerDeath(String name, String message) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("player", name);
        data.put("message", message);
        if (this.sendData("player_death", data)) this.logger.info("发送玩家死亡消息成功！");
        else this.logger.warning("发送玩家死亡消息失败！请检查机器人是否启动后再次尝试。");
    }

    public boolean sendSynchronousMessage(String message) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("message", message);
        return this.sendData("message", data);
    }


    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        this.logger.info("The event connection to the bot server was opened.");
    }

    @Override
    public void onMessage(String message) {
        this.message = message;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        this.logger.info("与机器人的连接已断开！");
    }

    @Override
    public void onError(Exception ex) {

    }
}