package com.app.webnest.mapper;

import com.app.webnest.domain.dto.PostSearchDTO;
import com.app.webnest.domain.vo.QuizVO;
import com.app.webnest.domain.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SearchMapper {
    public List<UserVO> selectByQuery(String query);

    public List<PostSearchDTO> selectQuestionPostQByQuery(String query);

    public List<PostSearchDTO> selectOpenPostQByQuery(String query);

    public List<QuizVO> selectQuizByQuery(String query);

    List<PostSearchDTO> selectQuestionPostCandidates(
            @Param("fullKeyword") String fullKeyword,
            @Param("keywordList") List<String> keywordList
    );

    List<PostSearchDTO> selectOpenPostCandidates(
            @Param("fullKeyword") String fullKeyword,
            @Param("keywordList") List<String> keywordList
    );
}
