package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class JoinRequestGetDTO {
    private Long id;
    private Long boardId;
    private Long senderId;
    private String senderUsername;
    private String boardName;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getBoardName() { return boardName; }
    public void setBoardName(String boardName) { this.boardName = boardName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}