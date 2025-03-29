package jiamingla.first.camera.market.dto;

public class TokenValidationResponse {
    private String message;
    private boolean isValid;

    public TokenValidationResponse(String message, boolean isValid) {
        this.message = message;
        this.isValid = isValid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }
}
