package com.app.webnest.service;

import com.app.webnest.domain.dto.QuizPersonalDTO;
import com.app.webnest.domain.dto.QuizResponseDTO;
import com.app.webnest.domain.vo.QuizPersonalVO;
import com.app.webnest.domain.vo.QuizVO;

import java.util.HashMap;
import java.util.List;

public interface QuizService {

    //    필터링, 페이징
    public List<QuizVO> quizDirection(HashMap<String, Object> params);

    //    전체 문제리스트
    public List<QuizVO> quizList();

    //    전체 문제수
    public Long quizCount(HashMap<String, Object> filters);

//    문제 조회
    public QuizVO findQuizById(Long id);

    public QuizPersonalDTO findQuizPersonalByAll();

//    결과 기대값조회
    public String findQuizExpectationById(Long id);


//    해당퀴즈에 대한 personal정보
    public QuizPersonalVO findQuizPersonalById(QuizResponseDTO quizResponseDTO);

    //    퀴즈 풀었던 내역저장
    public void saveQuizPersonal(QuizPersonalVO quizPersonalVO);

//    해당퀴즈 북마크여부
    public Integer isBookmarked(QuizResponseDTO quizResponseDTO);

//    해당퀴즈 해결여부
    public Integer isSolved(QuizResponseDTO quizResponseDTO );

//    회원탈퇴시 데이터삭제
    public void deleteQuizPersonal(Long id);


}
