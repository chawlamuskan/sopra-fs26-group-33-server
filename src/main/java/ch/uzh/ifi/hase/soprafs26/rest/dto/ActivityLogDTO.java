package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class ActivityLogDTO {
    private Long id;
    private Long userId;
    private String action;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
