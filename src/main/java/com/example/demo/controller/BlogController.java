package com.example.demo.controller;

import com.example.demo.dto.BlogDTO;
import com.example.demo.model.Blog;
import com.example.demo.model.User;
import com.example.demo.repository.BlogRepository;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;                 // <—
import org.springframework.security.core.context.SecurityContextHolder; // <—
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blogs")
public class BlogController {
    private final BlogRepository blogRepo;
    private final UserRepository userRepo;

    @GetMapping
    public List<Blog> list() {
        return blogRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return blogRepo.findById(id).map(b -> {
            // Xem blog: USER chỉ xem blog của mình, ADMIN xem tất
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            String currentUser = auth.getName();

            if (!isAdmin && !b.getAuthor().getUsername().equals(currentUser)) {
                return ResponseEntity.status(403).body("You can view only your own blog");
            }
            return ResponseEntity.ok(b);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody BlogDTO dto) {
        // Người tạo blog phải là chính mình (hoặc ADMIN có thể tạo hộ)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String currentUser = auth.getName();

        User author = userRepo.findById(dto.authorId()).orElse(null);
        if (author == null) return ResponseEntity.badRequest().body("authorId invalid");

        if (!isAdmin && !author.getUsername().equals(currentUser)) {
            return ResponseEntity.status(403).body("You can create blog only for yourself");
        }

        Blog b = Blog.builder()
                .title(dto.title())
                .content(dto.content())
                .author(author)
                .build();
        b = blogRepo.save(b);
        return ResponseEntity.created(URI.create("/api/blogs/" + b.getId())).body(b);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody BlogDTO dto) {
        return blogRepo.findById(id).map(b -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            String currentUser = auth.getName();

            if (!isAdmin && !b.getAuthor().getUsername().equals(currentUser)) {
                return ResponseEntity.status(403).body("You can update only your own blog");
            }

            // Nếu ADMIN muốn đổi tác giả, cho phép; còn USER thì phải giữ nguyên author chính mình
            if (!isAdmin && !dto.authorId().equals(b.getAuthor().getId())) {
                return ResponseEntity.status(403).body("Cannot change author");
            }

            b.setTitle(dto.title());
            b.setContent(dto.content());

            // ADMIN có thể set lại author theo dto (tuỳ yêu cầu, bạn có thể bỏ đoạn này)
            if (isAdmin && dto.authorId() != null) {
                userRepo.findById(dto.authorId()).ifPresent(b::setAuthor);
            }

            return ResponseEntity.ok(blogRepo.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return blogRepo.findById(id).map(b -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            String currentUser = auth.getName();

            if (!isAdmin && !b.getAuthor().getUsername().equals(currentUser)) {
                return ResponseEntity.status(403).body("You can delete only your own blog");
            }
            blogRepo.delete(b);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
