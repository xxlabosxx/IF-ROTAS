package com.example.ifrotas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Redireciona a raiz e as rotas do frontend para o index.html da SPA
    @GetMapping({"/", "/login", "/cadastro", "/feed", "/rotas/**", "/perfil", "/conexoes", "/chat/**"})
    public String home() {
        return "forward:/index.html";
    }
}
