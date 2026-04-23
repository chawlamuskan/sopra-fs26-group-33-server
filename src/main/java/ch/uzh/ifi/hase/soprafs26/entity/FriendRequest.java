package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import java.io.Serializable;

@Entity
@Table(name = "friendRequest")
public class FriendRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

    @ManyToOne 
    @JoinColumn(name = "senderId", nullable = false)
	private User sender;		

    @ManyToOne
    @JoinColumn(name = "receiverId", nullable = false)
	private User receiver;

    @Column(nullable = false)
	private FriendRequestStatus  status; 


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
  
    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }
    
    public FriendRequestStatus getStatus() {
        return status;
    }

    public void setStatus(FriendRequestStatus status) {
        this.status = status;
    }

}
