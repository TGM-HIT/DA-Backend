package at.ac.tgm.dto;

public class ErrorResponseDto {
    private String message;
    private int status;

    // Konstruktor
    public ErrorResponseDto(String message, int status) {
        this.message = message;
        this.status = status;
    }

    // Getter und Setter
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

