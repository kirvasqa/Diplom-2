package praktikum.models;

import lombok.Data;

@Data
public class JsonUserResponse {
    private boolean success;
    private User user;
    private String accessToken;
    private String refreshToken;
    private String message;
}

