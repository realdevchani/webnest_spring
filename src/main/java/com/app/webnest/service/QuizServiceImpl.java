package com.app.webnest.service;

import com.app.webnest.domain.dto.QuizPersonalDTO;
import com.app.webnest.domain.dto.QuizResponseDTO;
import com.app.webnest.domain.vo.QuizPersonalVO;
import com.app.webnest.domain.vo.QuizSubmitVO;
import com.app.webnest.domain.vo.QuizVO;
import com.app.webnest.exception.QuizException;
import com.app.webnest.repository.QuizDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional(rollbackFor=Exception.class)
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {


    private final QuizDAO quizDAO;

    @Override
    public List<QuizVO> quizDirection(HashMap<String, Object> params) {
        if (params == null) params = new HashMap<>();
        return quizDAO.selectQuizAll(params);
    }
    @Override
    public List<QuizPersonalDTO> findQuizPersonal(HashMap<String, Object> params) {
        return quizDAO.selectQuizWithPersonal(params);
    }

    @Override
    public List<QuizVO> quizList() { return quizDAO.selectAll(); }

    @Override
    public Long quizCount(HashMap<String, Object> filters) { return quizDAO.selectAllCount(filters); }

    @Override
    public QuizVO findQuizById(Long id) {
        QuizVO quizId = quizDAO.selectById(id);
        if(quizId == null){
            throw new QuizException("해당 문제 삭제");
        } else {
            return quizId;
        }
    }

    @Override
    public QuizPersonalDTO findQuizPersonalByAll(){
        return  quizDAO.selectQuizPersonalAll();
    }

    @Override
    public String findQuizExpectationById(Long id) {
        return quizDAO.selectExpectationById(id);
    }


    @Override
    public Integer isBookmarked(QuizResponseDTO quizResponseDTO) {
         return quizDAO.updateIsBookmark(quizResponseDTO);
        }
    @Override
    public Integer isSolved(QuizResponseDTO quizResponseDTO) {
        return quizDAO.updateIsSolve(quizResponseDTO);
    }
    @Override
    public QuizPersonalVO findQuizPersonalById(QuizResponseDTO quizResponseDTO) { return quizDAO.selectQuizPersonalById(quizResponseDTO); }

    @Override
    public void saveQuizPersonal(QuizPersonalVO quizPersonalVO) { quizDAO.insertQuizPersonal(quizPersonalVO); }

    @Override
    public void deleteQuizPersonal(Long id){}

    @Override
    public void saveQuizSubmit(QuizResponseDTO quizResponseDTO) { quizDAO.insertByQuizSubmit(quizResponseDTO); }

    @Override
    public QuizSubmitVO findQuizSubmitByIds(QuizResponseDTO quizResponseDTO) { return quizDAO.selectByQuizSubmit(quizResponseDTO); }

    @Override
    public List<QuizSubmitVO> findAllQuizSubmitByIds(QuizResponseDTO quizResponseDTO) { return quizDAO.selectByQuizSubmitAll(quizResponseDTO); }

    @Override
    public void modifySubmitResult(QuizResponseDTO quizResponseDTO) { quizDAO.updateBySubmitResult(quizResponseDTO); }


}
