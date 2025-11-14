package com.app.webnest.mapper;

import com.app.webnest.domain.dto.QuizPersonalDTO;
import com.app.webnest.domain.dto.QuizResponseDTO;
import com.app.webnest.domain.vo.QuizPersonalVO;
import com.app.webnest.domain.vo.QuizSubmitVO;
import com.app.webnest.domain.vo.QuizVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface QuizMapper {

    //    페이징, 필터링 처리
    public List<QuizVO> selectAllFilter(HashMap<String, Object> params);

    //    전체 문제리스트
    public List<QuizVO> selectAll();

    //    총 문제수
    public Long selectListTotalCount(HashMap<String, Object> filters);

//    퀴즈 조회
    public QuizVO select(Long id);

//    결과값 응답
    public String selectExpectation(Long quizId);

    public List<QuizPersonalDTO> selectQuizWithPersonal(HashMap<String, Object> params);

//    퀴즈JOIN 정보
    public QuizPersonalDTO selectQuizPersonalAll();

//    해당퀴즈에 대한 personal정보
    public QuizPersonalVO selectQuizPersonalById(QuizResponseDTO quizResponseDTO);

//    퀴즈 풀었던 내역저장
    public void insert(QuizPersonalVO QuizPersonalVO);

//    해당퀴즈 북마크업데이트
    public Integer updateIsBookmark(QuizResponseDTO  quizResponseDTO);

//    해당퀴즈 해결여부업데이트
    public Integer updateIsSolve(QuizResponseDTO   quizResponseDTO);

//    회원탈퇴시 데이터삭제
    public void delete(Long id);

    public <List>QuizPersonalDTO selectByBookmarkIsSolve(Long userId);

//    퀴즈 제출내역 추가
    public void insertQuizSubmit(QuizResponseDTO quizResponseDTO);

//    한사람의 해당문제에 대한 제출내역
    public QuizSubmitVO selectQuizSubmit(QuizResponseDTO quizResponseDTO);

//    한사람의 모든문제에 대한 제출내역들
    public List<QuizSubmitVO> selectQuizSubmitAll(QuizResponseDTO quizResponseDTO);

//    채점 후 정답이면 정답여부 업데이트
    public void updateSubmitResult(QuizResponseDTO quizResponseDTO);


}
