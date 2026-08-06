package br.com.geziel.family_finance.ping;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/ping")
@CrossOrigin(origins = "http://localhost:5173")
public class PingController {
    public record PingResponse(String message, LocalDateTime timestamp) {}

    @GetMapping
    public ResponseEntity<PingResponse> ping() {
        return ResponseEntity.ok(new PingResponse("pong", LocalDateTime.now()));
    }
}
