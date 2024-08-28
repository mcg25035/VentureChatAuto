package dev.mcloudtw.vca;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class MessageClassify {
    public enum MessageCategory {
        Warp,
        Trade,
        Task,
        Global
    }

    public CompletableFuture<MessageCategory> messageCategory = null;
    public String sender;
    public String message;

    private void boldClassify() {
        if (this.message.startsWith("#G") || this.message.startsWith("#g")) {
            messageCategory = CompletableFuture.completedFuture(MessageCategory.Global);
            message = message.substring(2);
            return;
        }
        if (this.message.startsWith("#TA") || this.message.startsWith("#ta")) {
            messageCategory = CompletableFuture.completedFuture(MessageCategory.Task);
            message = message.substring(3);
            return;
        }
        if (this.message.startsWith("#W") || this.message.startsWith("#w")) {
            messageCategory = CompletableFuture.completedFuture(MessageCategory.Warp);
            message = message.substring(2);
            return;
        }
        if (this.message.startsWith("#T") || this.message.startsWith("#t")) {
            messageCategory = CompletableFuture.completedFuture(MessageCategory.Trade);
            message = message.substring(2);
            return;
        }

        if (this.message.contains("pw")) {
            messageCategory = CompletableFuture.completedFuture(MessageCategory.Warp);
            return;
        }
        if (this.message.contains("res") && this.message.matches(".*(?:商|舖|賣|抽獎|賭場|雜貨|店|damn|貨).*")) {
            messageCategory = CompletableFuture.completedFuture(MessageCategory.Warp);
            return;
        }
        if (this.message.matches(".*(?:收|售|收購|出售|售出|買|賣|換)([1-9一二兩三四五六七八九零十]).*")) {
            messageCategory = CompletableFuture.completedFuture(MessageCategory.Trade);
            return;
        }
        if (this.message.contains("找人幫忙")) {
            messageCategory = CompletableFuture.completedFuture(MessageCategory.Task);
            return;
        }
        if (this.message.matches(".*徵([^\\n]+)工.*")) {
            messageCategory = CompletableFuture.completedFuture(MessageCategory.Task);
            return;
        }
        if (this.message.contains("誠徵")) {
            messageCategory = CompletableFuture.completedFuture(MessageCategory.Task);
            return;
        }
        if (this.message.contains("報酬")) {
            messageCategory = CompletableFuture.completedFuture(MessageCategory.Task);
            return;
        }
        if (this.message.contains("任務")) {
            messageCategory = CompletableFuture.completedFuture(MessageCategory.Task);
            return;
        }

        try{
            cautiousClassify();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        messageCategory = CompletableFuture.completedFuture(MessageCategory.Global);
    }

    private void cautiousClassify() throws Exception {
        URI uri = URI.create("https://mc.llm.codingbear.mcloudtw.com/getCategory");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", this.message);
        jsonObject.addProperty("sender", this.sender);
        HttpClient client = HttpClient.newBuilder()
                .sslContext(Utils.getUnsafeSslContext())
                .build();
        HttpRequest request = HttpRequest.newBuilder(uri)
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", Main.AUTH)
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                .build();
        this.messageCategory = CompletableFuture.supplyAsync(()->{
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    throw new IOException("Failed to classify message");
                }
                String body = response.body();
                Gson gson = new Gson();
                JsonObject responseJson = gson.fromJson(body, JsonObject.class);
                String category = responseJson.get("category").getAsString();
                if (category.equals("Unknown")) {
                    return MessageCategory.Global;
                }
                return MessageCategory.valueOf(category);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return MessageCategory.Global;
        });
    }

    public MessageClassify(String message, String sender) {
        this.sender = sender;
        this.message = message;
        this.boldClassify();
    }
}
