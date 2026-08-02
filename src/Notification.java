import java.sql.Timestamp;

public class Notification {
    private int notifId;
    private int userId;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {}
    public Notification(int notifId, int userId, String message, boolean isRead, Timestamp createdAt) {
        this.notifId = notifId;
        this.userId = userId;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public int getNotifId() { return notifId; }
    public void setNotifId(int notifId) { this.notifId = notifId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}