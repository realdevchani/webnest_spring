package com.app.webnest.service;

import com.app.webnest.domain.dto.PostSearchDTO;
import com.app.webnest.domain.vo.QuizVO;
import com.app.webnest.domain.vo.UserVO;
import com.app.webnest.exception.SearchException;
import com.app.webnest.repository.SearchDAO;
import com.app.webnest.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
@ToString
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private static final int TITLE_FULL_MATCH_SCORE = 10;
    private static final int CONTENT_FULL_MATCH_SCORE = 5;
    private static final int TITLE_KEYWORD_MATCH_SCORE = 2;
    private static final int CONTENT_KEYWORD_MATCH_SCORE = 1;

    private final SearchDAO searchDAO;



    @Override
    public List<PostSearchDTO> getOpenPostBySearchQuery(String searchQuery) {
        List<PostSearchDTO> foundOpenPosts = searchDAO.findSearchOpenPosts(searchQuery);
        if(foundOpenPosts.size() == 0){
            return new ArrayList<>();
        }
        return foundOpenPosts;
    }

    @Override
    public List<PostSearchDTO> getQuestionPostBySearchQuery(String searchQuery) {
        List<PostSearchDTO> foundQuestionPosts = searchDAO.findSearchQuestionPosts(searchQuery);
        if(foundQuestionPosts.size() == 0){
            return new ArrayList<>();
        }
        return foundQuestionPosts;
    }

    @Override
    public List<QuizVO> getQuizBySearchQuery(String searchQuery) {
        List<QuizVO> foundQuizzes = searchDAO.findSearchQuizzes(searchQuery);
        if(foundQuizzes.size() == 0){
            return new ArrayList<>();
        }
        return foundQuizzes;
    }

    @Override
    public List<UserVO> getUserBySearchQuery(String searchQuery) {
        List<UserVO> foundUsers = searchDAO.findSearchUsers(searchQuery);
        if(foundUsers.size() == 0){
            return new ArrayList<>();
        }
        return foundUsers;
    }

    @Override
    public List<PostSearchDTO> searchQuestionPosts(String query) {
        if (query == null) {
            throw new SearchException("검색어를 입력해주세요.");
        }

        String trimQuery = trimQuery(query);

        if(trimQuery.length() < 2){
            throw new SearchException("검색어는 2글자 이상부터 가능합니다.");
        }

        List<String> keywords = splitKeywords(trimQuery);
        List<PostSearchDTO> posts = searchDAO.findQuestionPostCandidates(trimQuery, keywords);

        setScore(posts, trimQuery);
        sortSearchList(posts);

        return posts;
    }

    @Override
    public List<PostSearchDTO> searchOpenPosts(String query) {
        if (query == null) {
            throw new SearchException("검색어를 입력해주세요.");
        }

        String trimQuery = trimQuery(query);

        if(trimQuery.length() < 2){
            throw new SearchException("검색어는 2글자 이상부터 가능합니다.");
        }

        List<String> keywords = splitKeywords(trimQuery);
        List<PostSearchDTO> posts = searchDAO.findOpenPostCandidates(trimQuery, keywords);

        setScore(posts, trimQuery);
        sortSearchList(posts);

        return posts;
    }

    public int scoreCalculator(PostSearchDTO post, String query){
        String trimQuery = trimQuery(query);

        String title = post.getPostTitle() == null ? "" : post.getPostTitle();
        String content = post.getPostContent() == null ? "" : post.getPostContent();
        List<String> keywords = splitKeywords(trimQuery);

        int score = 0;

        if(title.contains(trimQuery)){
            score += TITLE_FULL_MATCH_SCORE;
        }

        if(content.contains(trimQuery)){
            score += CONTENT_FULL_MATCH_SCORE;
        }

        for(String keyword : keywords){
            if(title.contains(keyword)){
                score += TITLE_KEYWORD_MATCH_SCORE;
            }

            if(content.contains(keyword)) {
                score += CONTENT_KEYWORD_MATCH_SCORE;
            }
        }

        return score;
    }

    public List<PostSearchDTO> setScore(List<PostSearchDTO> posts, String query){
        for (PostSearchDTO post : posts) {
            post.setScore(scoreCalculator(post, query));
        }
        return posts;
    }

    private String trimQuery(String query) {
        if (query == null) {
            return "";
        }
        return query.trim().replaceAll("\\s+", " ");
    }

    private List<String> splitKeywords(String query) {
        if (query.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(query.split(" "))
                .filter(keyword -> !keyword.isBlank())
                .distinct()
                .toList();
    }

    private List<PostSearchDTO> sortSearchList(List<PostSearchDTO> posts){
        posts.sort(
                Comparator.comparingInt(PostSearchDTO::getScore).reversed()
                        .thenComparing(PostSearchDTO::getPostCreateAt).reversed()
        );

        return posts;
    }

}
