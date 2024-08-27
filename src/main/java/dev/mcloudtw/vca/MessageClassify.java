package dev.mcloudtw.vca;

import java.util.concurrent.CompletableFuture;

public class MessageClassify {
    public enum MessageCategory {
        Warp,
        Trade,
        Task,
        Global
    }

    public CompletableFuture<MessageCategory> messageCategory = null;
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
        }

        messageCategory = CompletableFuture.completedFuture(MessageCategory.Global);
    }

    public MessageClassify(String message) {
        this.message = message;
        this.boldClassify();
    }
}
