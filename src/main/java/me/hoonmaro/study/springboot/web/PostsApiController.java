package me.hoonmaro.study.springboot.web;

import lombok.RequiredArgsConstructor;
import me.hoonmaro.study.springboot.service.posts.PostsService;
import me.hoonmaro.study.springboot.web.dto.PostsResponseDto;
import me.hoonmaro.study.springboot.web.dto.PostsSaveRequestDto;
import me.hoonmaro.study.springboot.web.dto.PostsUpdateRequestDto;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class PostsApiController {

    private final PostsService postsService;

    @GetMapping("/api/v1/posts/{id}")
    public PostsResponseDto findById(@PathVariable Long id) {
        return postsService.findById(id);
    }

    @PostMapping("/api/v1/posts")
    public Long save(@RequestBody PostsSaveRequestDto requestDto) {
        return postsService.save(requestDto);
    }

    @PutMapping("/api/v1/posts/{id}")
    public Long update(@PathVariable Long id, @RequestBody PostsUpdateRequestDto requestDto) {
        return postsService.update(id, requestDto);
    }

}
