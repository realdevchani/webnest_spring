package com.app.webnest.mapper;

import com.app.webnest.domain.dto.QuizResponseDTO;
import com.app.webnest.domain.vo.QuizPersonalVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class QuizMapperTest {

    @Autowired
    private QuizMapper quizMapper;

    @Test
    void selectQuizPersonalAllTest() {
        log.info("selectQuizPersonalAllTest {}", quizMapper.selectQuizPersonalAll());
    }

    @Test
    void selectQuizPersonalByIdTest() {
    }

    @Test
    void insert() {
        QuizPersonalVO quizPersonalVO = new QuizPersonalVO();
        quizPersonalVO.setId(2L);
        quizPersonalVO.setQuizId(2L);
        quizPersonalVO.setUserId(2L);
        quizMapper.insert(quizPersonalVO);
    }


    @Test
    void delete() {
    }

    @Test
    void insertQuizSubmitTest() {
        QuizResponseDTO quizResponseDTO = new QuizResponseDTO();
        quizResponseDTO.setQuizId(2L);
        quizResponseDTO.setUserId(2L);
        quizMapper.insertQuizSubmit(quizResponseDTO);
    }

    @Test
    void selectQuizSubmitTest() {
        QuizResponseDTO quizResponseDTO = new QuizResponseDTO();
        quizResponseDTO.setQuizId(2L);
        quizResponseDTO.setUserId(2L);
        log.info("Submit: {}",quizMapper.selectQuizSubmit(quizResponseDTO));
    }

    @Test
    void selectQuizSubmitAllTest() {
        QuizResponseDTO quizResponseDTO = new QuizResponseDTO();
        quizResponseDTO.setUserId(2L);
        log.info("SubmitAll: {}",quizMapper.selectQuizSubmitAll(quizResponseDTO));
    }

    @Test
    void updateSubmitResultTest() {
        QuizResponseDTO quizResponseDTO = new QuizResponseDTO();
        quizResponseDTO.setUserId(2L);
        quizMapper.updateSubmitResult(quizResponseDTO);
    }
    @Test
    void selectByBookmarkIsSolveTest(){
        QuizResponseDTO quizResponseDTO = new QuizResponseDTO();
        quizResponseDTO.setUserId(1L);
        log.info("북마크해결여부: {}",quizMapper.selectByBookmarkIsSolve(1L));
    }
}