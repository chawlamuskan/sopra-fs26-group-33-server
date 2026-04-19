package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.InviteStatus;

public class InvitationGetDTO {

    private Long id;

    private Long boardId;

    private Long senderId;
    
    private Long receiverId;

    private InviteStatus status;
    
    private String boardName;
    
    private String senderUsername;
			

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBoardId() {
		return boardId;
	}

	public void setBoardId(Long boardId) {
		this.boardId = boardId;
    }

    public Long getSenderId() {
		return senderId;
	}

	public void setSenderId(Long senderId) {
		this.senderId = senderId;
	}

	public Long getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(Long receiverId) {
		this.receiverId = receiverId;
	}

    public InviteStatus getStatus() {
		return status;
	}

    public void setStatus(InviteStatus status) {
        this.status = status;
    }

    public String getBoardName() {
        return boardName;
    }
    
    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }
    
    public String getSenderUsername() {
        return senderUsername;
    }
    
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

}