package com.expense.ExpenseTracker.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmailMessage {
    private String to;
    private String subject;
    private String content;
    private byte[] attachment;
    private String attachmentName;
    
    public EmailMessage(String to, String subject, String content, byte[] attachment) {
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.attachment = attachment;
        this.attachmentName = "expense_report.pdf";
    }
}
