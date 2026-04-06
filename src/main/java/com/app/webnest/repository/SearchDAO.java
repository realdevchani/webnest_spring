package com.app.webnest.repository;

import com.app.webnest.domain.dto.PostSearchDTO;
import com.app.webnest.domain.vo.QuizVO;
import com.app.webnest.domain.vo.UserVO;
import com.app.webnest.mapper.SearchMapper;
import com.app.webnest.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SearchDAO {
    private final SearchMapper searchMapper;

    public List<UserVO> findSearchUsers(String query) {
        return searchMapper.selectByQuery(query);
    }
    public List<PostSearchDTO> findSearchOpenPosts(String query) {
        return searchMapper.selectOpenPostQByQuery(query);
    }
    public List<PostSearchDTO> findSearchQuestionPosts(String query) {
        return searchMapper.selectQuestionPostQByQuery(query);
    }
    public List<QuizVO> findSearchQuizzes(String query) {
        return searchMapper.selectQuizByQuery(query);
    }

    public List<PostSearchDTO> findQuestionPostCandidates(String fullKeyword, List<String> keywordList) {
        return searchMapper.selectQuestionPostCandidates(fullKeyword, keywordList);
    }

    public List<PostSearchDTO> findOpenPostCandidates(String fullKeyword, List<String> keywordList) {
        return searchMapper.selectOpenPostCandidates(fullKeyword, keywordList);
    }
}