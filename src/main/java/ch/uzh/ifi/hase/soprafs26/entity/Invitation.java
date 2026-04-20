package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import ch.uzh.ifi.hase.soprafs26.constant.InviteStatus;

import java.io.Serializable;

@Entity
@Table(name = "invitation")
public class Invitation implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

    @ManyToOne 
	@JoinColumn(name = "boardId", nullable = false)
	private TravelBoard board;

    @ManyToOne // a sender can send multiple invtations
	private User sender;		

    @ManyToOne // a receiver can receive multiple invtations
	private User receiver;

    @Column(nullable = false)
	private InviteStatus status; 


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
 
	public TravelBoard getBoard() {
        return board;
    }

    public void setBoard(TravelBoard board) {
        this.board = board;
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
    
    public InviteStatus getStatus() {
        return status;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }
}
