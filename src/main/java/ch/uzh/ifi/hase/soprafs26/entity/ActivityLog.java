package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "activity_logs")
public class ActivityLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private TravelBoard board;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String action;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TravelBoard getBoard() { return board; }
    public void setBoard(TravelBoard board) { this.board = board; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}