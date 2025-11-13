package com.app.webnest.mapper;

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
}