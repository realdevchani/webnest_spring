package com.app.webnest.repository;

import com.app.webnest.domain.dto.QuizResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest
@Slf4j
class QuizDAOTest {

    @Autowired
    private QuizDAO quizDAO;

    @Test
    void selectQuizAll() {
        HashMap<String,Object> map = new HashMap<>();
        map.put("quizDifficult", "중급");
        map.put("quizLanguage", "ORACLE");
        map.put("cursor", "1");

    }

    @Test
    void selectAllCountTest() {
        HashMap<String,Object> map = new HashMap<>();
        log.info("selectAllCount: {}", quizDAO.selectAllCount(map));
    }

    @Test
    public void selectByIdTest() {
        log.info("selectById: {}", quizDAO.selectById(1L));
    }

    @Test
    public void selectByQuizExpectationTest() {
        log.info("expectation {}", quizDAO.selectExpectationById(1L));
    }


    @Test
    void insertQuizPersonalTest() {
    }


    @Test
    void deleteQuizPersonalTest() {
    }

    @Test
    void insertByQuizSubmitTest() {
        QuizResponseDTO quizResponseDTO = new QuizResponseDTO();
        quizResponseDTO.setQuizId(4L);
        quizResponseDTO.setUserId(5L);
        quizDAO.insertByQuizSubmit(quizResponseDTO);
    }

    @Test
    void selectByQuizSubmitTest() {
        QuizResponseDTO quizResponseDTO = new QuizResponseDTO();
        quizResponseDTO.setQuizId(4L);
        quizResponseDTO.setUserId(5L);
        log.info("submit: {}",  quizDAO.selectByQuizSubmit(quizResponseDTO));
    }

    @Test
    void selectByQuizSubmitAllTest() {
        QuizResponseDTO quizResponseDTO = new QuizResponseDTO();
        quizResponseDTO.setUserId(5L);
        log.info("submitAll: {}", quizDAO.selectByQuizSubmitAll(quizResponseDTO));
    }

    @Test
    void updateBySubmitResultTest() {
        QuizResponseDTO quizResponseDTO = new QuizResponseDTO();
        quizResponseDTO.setQuizId(4L);
        quizResponseDTO.setUserId(5L);
        quizDAO.updateBySubmitResult(quizResponseDTO);
    }
}