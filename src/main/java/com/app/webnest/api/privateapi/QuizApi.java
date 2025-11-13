package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.dto.QuizPersonalDTO;
import com.app.webnest.domain.dto.QuizResponseDTO;
import com.app.webnest.domain.vo.QuizPersonalVO;
import com.app.webnest.domain.vo.QuizVO;
import com.app.webnest.exception.GlobalExceptionHandler;
import com.app.webnest.exception.QuizException;
import com.app.webnest.service.JavaCompileService;
import com.app.webnest.service.QuizService;
import com.app.webnest.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/*")
@RequiredArgsConstructor
@Slf4j
public class QuizApi {

    private final JavaCompileService javaCompileService;
    private final QuizService quizService;
    private final UserService userService;

    @PostMapping("/quiz")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getQuizList(@RequestBody(required = false) Map<String,Object> params) {
        // 쿼리에 넘길 Map 구성

        if (params == null) params = new HashMap<>();
//        전체 문제 수
        // 안전한 파싱 및 기본값
        String quizLanguage = params.get("quizLanguage") == null ? null : String.valueOf(params.get("quizLanguage"));
        String quizDifficult = params.get("quizDifficult") == null ? null : String.valueOf(params.get("quizDifficult"));
        String keyword = params.get("keyword") == null ? null : String.valueOf(params.get("keyword"));

        int page = 1;
        if (params.get("page") != null) {
            try {
                page = Integer.parseInt(String.valueOf(params.get("page"))); // 들어오는 현재페이지번호 ex) Object타입의 "1" String으로 형변환후 Integer로 다시 형변환해서 처리
                if (page < 1) page = 1;
            } catch (NumberFormatException ignored) {}
        }

//                 화면에서 받아올 값
        HashMap<String, Object> filters = new HashMap<>();
        filters.put("quizLanguage", quizLanguage);
        filters.put("quizDifficult", quizDifficult);
        filters.put("keyword", keyword);
        filters.put("page", page); // 매퍼에서 page로 OFFSET 계산

        List<QuizVO> findQuizList = quizService.quizDirection(filters); // service에서 매퍼 호출
        if (findQuizList == null) findQuizList = new ArrayList<>();
        Long quizTotalCount = quizService.quizCount(filters);
        Map<String,Object> data = new HashMap<>();
        data.put("findQuizList", findQuizList);
        data.put("quizTotalCount", quizTotalCount);

        return ResponseEntity.ok(ApiResponseDTO.of("문제리스트 불러오기", data));

    };

    @PostMapping("/workspace/quiz/{id}")
    public ResponseEntity<ApiResponseDTO<HashMap>> getQuizById(@RequestBody QuizResponseDTO quizResponseDTO) {
        HashMap <String, Object> quizDatas = new HashMap<>();
        Long findQuizId = quizResponseDTO.getQuizId();
        Long findUserId = quizResponseDTO.getUserId();

        QuizVO findQuiz = quizService.findQuizById(findQuizId);
        QuizPersonalVO quizPersonalVO = new QuizPersonalVO();
        quizPersonalVO.setUserId(findUserId);
        quizPersonalVO.setQuizId(findQuizId);

        quizService.saveQuizPersonal(quizPersonalVO);

        quizDatas.put("findQuiz", findQuiz);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("문제상세조회",  quizDatas));
    }

    @PostMapping("/quiz/all")
    public ResponseEntity<ApiResponseDTO<List<QuizVO>>> getAllQuizList() {
        List<QuizVO>quizList = quizService.quizList();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("전체문제조회", quizList));
    }

    @PostMapping("/quiz/js-success")
    public ResponseEntity<ApiResponseDTO<String>> getJsQuizSuccess(@RequestBody QuizResponseDTO quizResponseDTO) {
        QuizResponseDTO dto = new QuizResponseDTO();
        Long findUserId = quizResponseDTO.getUserId();
        Long findQuizId = quizResponseDTO.getQuizId();
        QuizVO findQuiz = quizService.findQuizById(findQuizId);
        Integer findQuizExp = findQuiz.getQuizExp();

        dto.setUserId(findUserId);
        dto.setQuizId(findQuizId);

        quizService.isSolved(dto);
        userService.gainExp(findUserId, findQuizExp);


        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!"));
    }

    @PostMapping("/quiz/java-expectation")
    public ResponseEntity<ApiResponseDTO<String>> getJavaExpectation(@RequestBody QuizResponseDTO quizResponseDTO ) {
        Long findUserId = quizResponseDTO.getUserId();
        Long findQuizId = quizResponseDTO.getQuizId();

        String code = quizResponseDTO.getCode();
        String className = quizResponseDTO.getClassName();

        if(code == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseDTO.of("잘못된 요청"));
        }
        String result = javaCompileService.execute(className, code);

        if(result == null) {
            throw new QuizException("컴파일오류");
        }
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("실행 성공", result));
    }

    @PostMapping("/quiz/sql-expectation")
    public ResponseEntity<ApiResponseDTO<String>> getSqlExpectation(@RequestBody QuizResponseDTO quizResponseDTO ) {
        Long findQuizId = quizResponseDTO.getQuizId();
        Long findUserId = quizResponseDTO.getUserId();

        String getUserCode = quizResponseDTO.getCode();
        String quizExpectation = quizService.findQuizExpectationById(findQuizId).toUpperCase();

        if(!getUserCode.equals(quizExpectation)) {
           throw new QuizException("잘못된 쿼리");
        }
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("실행 성공", quizExpectation));
    }

    @PostMapping("/quiz/java-success")
    public ResponseEntity<ApiResponseDTO<HashMap>> getIsSuccess(@RequestBody QuizResponseDTO quizResponseDTO ) {
        HashMap<String, Object> data = new HashMap<>();
        String code = quizResponseDTO.getCode();
        String className = quizResponseDTO.getClassName();

        QuizResponseDTO dto = new QuizResponseDTO();
        Long quizId = quizResponseDTO.getQuizId();
        Long userId = quizResponseDTO.getUserId();
        dto.setQuizId(quizId);
        dto.setUserId(userId);

        quizService.isSolved(dto);

        QuizVO findQuiz = quizService.findQuizById(quizId);
        String findQuizExpectation = findQuiz.getQuizExpectation();
        Integer findQuizExp = findQuiz.getQuizExp();

        String result = javaCompileService.execute(className, code);

        if(result == null) {
            throw new QuizException("소스코드를 다시 확인해주세요");
        }

        if(!result.equals(findQuizExpectation)){
            throw new QuizException("기댓값과 일치하지 않습니다. 다시 시도해보세요!");
        }
        userService.gainExp(userId, findQuizExp);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!", data));
    }

    @PostMapping("/quiz/sql-success")
    public ResponseEntity<ApiResponseDTO<HashMap>> getSqlSuccess(@RequestBody QuizResponseDTO quizResponseDTO ) {
        HashMap<String, Object> data = new HashMap<>();
        QuizResponseDTO dto = new QuizResponseDTO();
        Long findQuizId = quizResponseDTO.getQuizId();
        Long findUserId = quizResponseDTO.getUserId();
        dto.setQuizId(findQuizId);
        dto.setUserId(findUserId);
        quizService.isSolved(dto);

        String code = quizResponseDTO.getCode();
        String quizExpectation = quizService.findQuizExpectationById(findQuizId).toUpperCase();

        QuizVO findQuiz = quizService.findQuizById(findQuizId);
        Integer findQuizExp = findQuiz.getQuizExp();
        if(!code.equals(quizExpectation)) {
            throw new QuizException("잘못된 쿼리 ex) 소문자입력, 세미콜론 미작성");
        }
        data.put("QuizExp", findQuizExp);
        userService.gainExp(findUserId, findQuizExp);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!", data));
    }

}
