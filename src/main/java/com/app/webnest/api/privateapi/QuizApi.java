package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.domain.dto.QuizPersonalDTO;
import com.app.webnest.domain.dto.QuizResponseDTO;
import com.app.webnest.domain.dto.UserResponseDTO;
import com.app.webnest.domain.vo.QuizPersonalVO;
import com.app.webnest.domain.vo.QuizSubmitVO;
import com.app.webnest.domain.vo.QuizVO;
import com.app.webnest.exception.GlobalExceptionHandler;
import com.app.webnest.exception.QuizException;
import com.app.webnest.service.JavaCompileService;
import com.app.webnest.service.QuizService;
import com.app.webnest.service.UserService;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/*")
@RequiredArgsConstructor
@Slf4j
public class QuizApi {

    private final JavaCompileService javaCompileService;
    private final QuizService quizService;
    private final UserService userService;


    @PostMapping("/quiz")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getQuizList(@RequestBody Map<String,Object> params, QuizResponseDTO quizResponseDTO) {
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

        Long userId = quizResponseDTO != null ? quizResponseDTO.getUserId() : null;

//                 화면에서 받아올 값
        HashMap<String, Object> filters = new HashMap<>();
        filters.put("quizLanguage", quizLanguage);
        filters.put("quizDifficult", quizDifficult);
        filters.put("keyword", keyword);
        filters.put("page", page); // 매퍼에서 page로 OFFSET 계산
        filters.put("userId", userId);

        List<QuizPersonalDTO> findQuizList = quizService.findQuizPersonal(filters); // service에서 매퍼 호출
        if (findQuizList == null) findQuizList = new ArrayList<>();


        Long quizTotalCount = quizService.quizCount(filters);
        Map<String,Object> data = new HashMap<>();
        data.put("findQuizList", findQuizList);
        data.put("quizTotalCount", quizTotalCount);
        data.put("page", page);

        return ResponseEntity.ok(ApiResponseDTO.of("문제리스트 불러오기", data));
    };

    @PostMapping("/quiz/{quizId}/bookmark")
    public ResponseEntity<ApiResponseDTO<HashMap>> getQuizBookmarkAndIsSolve(
            @RequestBody QuizResponseDTO quizResponseDTO,
            @PathVariable("quizId") Long quizId
    ) {

        try {
        log.info("toggleBookmark called pathQuizId={}, rawBodyPresent={}", quizId, /* no direct raw body */ true);
        HashMap<String, Object> data = new HashMap<>();
        if(quizResponseDTO == null || quizResponseDTO.getUserId() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDTO.of("유저 아이디가 필요함", null));
        }
        quizResponseDTO.setQuizId(quizId);
        Long userId = quizResponseDTO.getUserId();

        QuizResponseDTO dto = new QuizResponseDTO();
        dto.setUserId(userId);
        dto.setQuizId(quizId);
            quizService.isBookmarked(dto);

            // 최신 상태 조회
            QuizPersonalVO quizPersonalVO = quizService.findQuizPersonalById(dto);
            if (quizPersonalVO == null) {
                quizPersonalVO = new QuizPersonalVO();
                quizPersonalVO.setUserId(userId);
                quizPersonalVO.setQuizId(quizId);
                quizPersonalVO.setQuizPersonalIsBookmark(0);
                quizPersonalVO.setQuizPersonalIsSolve(0);
                quizService.saveQuizPersonal(quizPersonalVO);
            }

            data.put("quizPersonal", quizPersonalVO);
            log.info("isBookmarked returned: {}", quizPersonalVO.getQuizPersonalIsBookmark());




        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("응답 성공", data));
        } catch (Exception e) {
            log.error("isBookmarked failed: {}", e.toString(), e); // 전체 스택트레이스 로그
            Throwable cause = e.getCause();
            while (cause != null) {
                log.error("Caused by: {}", cause.toString());
                cause = cause.getCause();
            }
            throw e; // 또는 적절한 에러 응답 반환
        }
    }


    @PostMapping("/workspace/quiz/{id}")
    public ResponseEntity<ApiResponseDTO<HashMap>> getQuizById(@RequestBody QuizResponseDTO quizResponseDTO) {
        HashMap <String, Object> quizDatas = new HashMap<>();
        Long findQuizId = quizResponseDTO.getQuizId();
        Long findUserId = quizResponseDTO.getUserId();

        QuizVO findQuiz = quizService.findQuizById(findQuizId);
        QuizPersonalVO quizPersonalVO = new QuizPersonalVO();
        quizPersonalVO.setUserId(findUserId);
        quizPersonalVO.setQuizId(findQuizId);


        quizDatas.put("findQuiz", findQuiz);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("문제상세조회",  quizDatas));
    }

    @PostMapping("/quiz/all")
    public ResponseEntity<ApiResponseDTO<List<QuizVO>>> getAllQuizList() {
        List<QuizVO>quizList = quizService.quizList();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("전체문제조회", quizList));
    }

//    서브밋 테이블에 사용자의 코드,유저아이디, 퀴즈아이디, 제출시간, 디폴트에러
    @PostMapping("/quiz/js-success")
    public ResponseEntity<ApiResponseDTO<HashMap>> getJsQuizSuccess(@RequestBody QuizResponseDTO quizResponseDTO) {
        QuizResponseDTO dto = new QuizResponseDTO();
        HashMap<String, Object> submitDatas = new HashMap<>();
        Long findUserId = quizResponseDTO.getUserId();
        Long findQuizId = quizResponseDTO.getQuizId();
        String userCode = quizResponseDTO.getQuizSubmitCode();

        log.info("finduserCode: {}", userCode);
        log.info("findUserId: {}",findUserId);
        log.info("findQuizId: {}",findQuizId);

        QuizPersonalVO findQuizPersonalVO = new QuizPersonalVO();
        findQuizPersonalVO.setUserId(findUserId);
        findQuizPersonalVO.setQuizId(findQuizId);

        QuizVO findQuiz = quizService.findQuizById(findQuizId);
        Integer findQuizExp = findQuiz.getQuizExp();

        dto.setUserId(findUserId);
        dto.setQuizId(findQuizId);
        dto.setQuizSubmitCode(userCode);
//        서브밋 테이블에 추가해야함
        quizService.saveQuizSubmit(dto);
        quizService.modifySubmitResult(dto);
//        퍼스널 테이블에 추가
        quizService.saveQuizPersonal(findQuizPersonalVO);
        Integer isSolved = quizService.isSolved(dto);
        userService.gainExp(findUserId, findQuizExp);


        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!",submitDatas));
    }

//    자바실행
    @PostMapping("/quiz/java-expectation")
    public ResponseEntity<ApiResponseDTO<String>> getJavaExpectation(@RequestBody QuizResponseDTO quizResponseDTO ) {
        Long findUserId = quizResponseDTO.getUserId();
        Long findQuizId = quizResponseDTO.getQuizId();

        String code = quizResponseDTO.getQuizSubmitCode();
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

//    sql실행
    @PostMapping("/quiz/sql-expectation")
    public ResponseEntity<ApiResponseDTO<String>> getSqlExpectation(@RequestBody QuizResponseDTO quizResponseDTO ) {
        Long findQuizId = quizResponseDTO.getQuizId();
        Long findUserId = quizResponseDTO.getUserId();

        String getUserCode = quizResponseDTO.getQuizSubmitCode();
        String quizExpectation = quizService.findQuizExpectationById(findQuizId).toUpperCase();

        if(!getUserCode.equals(quizExpectation)) {
           throw new QuizException("잘못된 쿼리");
        }
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("실행 성공", quizExpectation));
    }
//  자바 채점
    @PostMapping("/quiz/java-success")
    public ResponseEntity<ApiResponseDTO<HashMap>> getIsSuccess(@RequestBody QuizResponseDTO quizResponseDTO ) {
        HashMap<String, Object> data = new HashMap<>();
        String code = quizResponseDTO.getQuizSubmitCode();
        String className = quizResponseDTO.getClassName();

        QuizResponseDTO dto = new QuizResponseDTO();
        Long quizId = quizResponseDTO.getQuizId();
        Long userId = quizResponseDTO.getUserId();
        dto.setQuizId(quizId);
        dto.setUserId(userId);
        dto.setQuizSubmitCode(code);

        QuizPersonalVO findQuizPersonalVO = new QuizPersonalVO();
        findQuizPersonalVO.setUserId(userId);
        findQuizPersonalVO.setQuizId(quizId);

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
        quizService.saveQuizSubmit(dto);
        quizService.modifySubmitResult(dto);
        quizService.saveQuizPersonal(findQuizPersonalVO);
        quizService.isSolved(dto);
        userService.gainExp(userId, findQuizExp);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!", data));
    }
//    sql채점
    @PostMapping("/quiz/sql-success")
    public ResponseEntity<ApiResponseDTO<HashMap>> getSqlSuccess(@RequestBody QuizResponseDTO quizResponseDTO ) {
        HashMap<String, Object> data = new HashMap<>();
        QuizResponseDTO dto = new QuizResponseDTO();
        Long findQuizId = quizResponseDTO.getQuizId();
        Long findUserId = quizResponseDTO.getUserId();
        String findCode = quizResponseDTO.getQuizSubmitCode();

        dto.setQuizId(findQuizId);
        dto.setUserId(findUserId);
        dto.setQuizSubmitCode(findCode);

        QuizPersonalVO findQuizPersonalVO = new QuizPersonalVO();
        findQuizPersonalVO.setUserId(findUserId);
        findQuizPersonalVO.setQuizId(findQuizId);

        quizService.isSolved(dto);

        String code = quizResponseDTO.getQuizSubmitCode();
        String quizExpectation = quizService.findQuizExpectationById(findQuizId).toUpperCase();

        QuizVO findQuiz = quizService.findQuizById(findQuizId);
        Integer findQuizExp = findQuiz.getQuizExp();
        if(!code.equals(quizExpectation)) {
            throw new QuizException("잘못된 쿼리 ex) 소문자입력, 세미콜론 미작성");
        }

        quizService.saveQuizSubmit(dto);
        quizService.modifySubmitResult(dto);
        quizService.saveQuizPersonal(findQuizPersonalVO);
        quizService.isSolved(dto);
        userService.gainExp(findUserId, findQuizExp);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!", data));
    }
}
