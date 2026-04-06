package com.app.webnest.service;


import com.app.webnest.domain.dto.PostSearchDTO;
import com.app.webnest.domain.vo.QuizVO;
import com.app.webnest.domain.vo.UserVO;

import java.util.List;

public interface SearchService {

    public List<PostSearchDTO> getOpenPostBySearchQuery(String searchQuery);

    public List<PostSearchDTO> getQuestionPostBySearchQuery(String searchQuery);

    public List<QuizVO> getQuizBySearchQuery(String searchQuery);

    public List<UserVO> getUserBySearchQuery(String searchQuery);

    public List<PostSearchDTO> searchQuestionPosts(String rawKeyword);

    public List<PostSearchDTO> searchOpenPosts(String rawKeyword);
}
