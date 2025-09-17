package com.Finanza.backend.gemini;

public class SummarizeRequest {
    private String text;
    private int wordCount;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }
}