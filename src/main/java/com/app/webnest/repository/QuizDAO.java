package com.app.webnest.repository;

import com.app.webnest.domain.dto.QuizPersonalDTO;
import com.app.webnest.domain.dto.QuizResponseDTO;
import com.app.webnest.domain.vo.QuizPersonalVO;
import com.app.webnest.domain.vo.QuizVO;
import com.app.webnest.mapper.QuizMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QuizDAO {

    private final QuizMapper quizMapper;

//    페이징, 필터링
    public List<QuizVO> selectQuizAll(HashMap<String, Object> params){
        return quizMapper.selectAllFilter(params);
    }

//    전체 문제리스트
    public List<QuizVO> selectAll(){ return  quizMapper.selectAll(); }

//    전체 문제수
    public Long selectAllCount(HashMap<String, Object> filters){ return quizMapper.selectListTotalCount(filters); }

//    문제 조회
    public QuizVO selectById(Long id) { return quizMapper.select(id); }

//    문제 기댓값
    public String selectExpectationById(Long quizId) { return quizMapper.selectExpectation(quizId); }

//    퀴즈리드 (join)
    public QuizPersonalDTO selectQuizPersonalAll() { return  quizMapper.selectQuizPersonalAll(); }

    //    해당퀴즈에 대한 personal정보
    public QuizPersonalVO  selectQuizPersonalById(QuizResponseDTO quizResponseDTO) { return quizMapper.selectQuizPersonalById(quizResponseDTO); }

    //    퀴즈 풀었던 내역저장
    public void insertQuizPersonal(QuizPersonalVO quizPersonalVO) { quizMapper.insert(quizPersonalVO); }

//    해당퀴즈 북마크여부
    public Integer updateBookmark(QuizResponseDTO quizResponseDTO){ return quizMapper.updateIsBookmark(quizResponseDTO); }

//    해당퀴즈 해결여부
    public Integer updateIsSolve(QuizResponseDTO quizResponseDTO){  return quizMapper.updateIsSolve(quizResponseDTO); }

//    회원탈퇴시 데이터삭제
    public void deleteQuizPersonal(Long id){}

}
