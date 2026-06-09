package com.proyecto.TurboMechanics.dto;

public class ChatbotResponseDTO {

    private String reply;
    private String role;

    public ChatbotResponseDTO(String reply, String role) {
        this.reply = reply;
        this.role = role;
    }

    public String getReply() { 
        return reply; 
    }

    public String getRole() { 
        return role;  
    }
}