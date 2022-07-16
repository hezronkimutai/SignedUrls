package com.hez.kim;

import lombok.Value;

@Value
public class Response {
    Boolean success;
    String[] error_codes;
    String access_token;
    String accessToken;
    String error;
    String error_description;
}
