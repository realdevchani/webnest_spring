package com.app.webnest.api.publicapi;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.dto.PostSearchDTO;
import com.app.webnest.domain.vo.QuizVO;
import com.app.webnest.domain.vo.UserVO;
import com.app.webnest.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchApi {
    private final SearchService searchService;

    public Map<String, Object> createEmptyResult(String query){
        Map<String, Object> result = new HashMap<>();
        result.put("search", query);
        result.put("openPosts", new ArrayList<>());
        result.put("questionPosts", new ArrayList<>());
        result.put("quizzes", new ArrayList<>());
        result.put("users", new ArrayList<>());
        return result;
    }
    public boolean isBlankQuery(String query){
        boolean result = false;
        if(query == null || query.length() == 0){
            result = true;
        }
        return result;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO> searchResult(@RequestParam(value = "search", required = false) String query) {
        Map<String, Object> result = createEmptyResult(query);
        if (isBlankQuery(query)) {
            return ResponseEntity.ok(ApiResponseDTO.of("검색 결과", result));
        }

        String trimQuery = query.trim();

        List<QuizVO> quizzes = searchService.getQuizBySearchQuery(trimQuery);
        List<UserVO> users = searchService.getUserBySearchQuery(trimQuery);
        List<PostSearchDTO> openPosts = searchService.searchOpenPosts(trimQuery);
        List<PostSearchDTO> questionPosts = searchService.searchQuestionPosts(trimQuery);

        result.replace("openPosts", openPosts);
        result.replace("questionPosts", questionPosts);
        result.replace("quizzes", quizzes);
        result.replace("users", users);

        return ResponseEntity.ok(ApiResponseDTO.of("검색 결과", result));
    }
}
