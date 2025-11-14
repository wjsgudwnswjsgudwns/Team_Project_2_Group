package com.example.test.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home2")
public class HomeController {

    @Autowired
    private HomeService home2Service;

    @GetMapping("/recent")
    public ResponseEntity<Home2Dto> getRecentPosts() {
        Home2Dto response = home2Service.getRecentPosts();
        return ResponseEntity.ok(response);
    }
}
