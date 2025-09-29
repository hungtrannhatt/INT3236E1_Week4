package com.example.demo.controller;

import com.example.demo.dto.BlogRequest;
import com.example.demo.model.Blog;
import com.example.demo.model.User;
import com.example.demo.repository.BlogRepository;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Blog> get(@PathVariable Long id) {
        return blogRepo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody BlogRequest req) {
        User author = userRepo.findById(req.authorId()).orElse(null);
        if (author == null) return ResponseEntity.badRequest().body("authorId invalid");

        Blog b = Blog.builder()
                .title(req.title())
                .content(req.content())
                .author(author)
                .build();
        b = blogRepo.save(b);
        return ResponseEntity.created(URI.create("/api/blogs/" + b.getId())).body(b);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody BlogRequest req) {
        return blogRepo.findById(id).map(b -> {
            User author = userRepo.findById(req.authorId()).orElse(null);
            if (author == null) return ResponseEntity.badRequest().body("authorId invalid");
            b.setTitle(req.title());
            b.setContent(req.content());
            b.setAuthor(author);
            return ResponseEntity.ok(blogRepo.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!blogRepo.existsById(id)) return ResponseEntity.notFound().build();
        blogRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
