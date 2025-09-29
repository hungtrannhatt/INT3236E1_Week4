package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <—
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder; // thêm dependency injection

    @GetMapping
    public List<User> list() {
        return userRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable Long id) {
        return userRepo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody UserDTO dto) {
        if (userRepo.existsByUsername(dto.username())) {
            return ResponseEntity.badRequest().build();
        }
        User u = User.builder()
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password())) // MÃ HOÁ LUÔN
                .role(Role.valueOf("USER"))             // bài 2: có role. Có thể set ADMIN thủ công khi seed
                .build();
        u = userRepo.save(u);
        return ResponseEntity.created(URI.create("/api/users/" + u.getId())).body(u);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        return userRepo.findById(id).map(u -> {
            u.setUsername(dto.username());
            u.setPassword(dto.password());
            if (dto.password() != null && !dto.password().isBlank()) {
                u.setPassword(passwordEncoder.encode(dto.password())); // MÃ HOÁ KHI ĐỔI
            }
            return ResponseEntity.ok(userRepo.save(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ====== CHỐT QUYỀN: chỉ ADMIN được xoá ======
    @PreAuthorize("hasRole('ADMIN')") // <—
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!userRepo.existsById(id)) return ResponseEntity.notFound().build();
        userRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
