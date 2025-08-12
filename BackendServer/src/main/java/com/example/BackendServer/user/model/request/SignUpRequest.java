package com.example.BackendServer.user.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
  private String id;
  private String password;
  private String email;
  private String role;
}
