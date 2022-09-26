package com.james.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class FakeLoginController {

    @PostMapping("/user/b/login")
    public void Login() {

    }
}
