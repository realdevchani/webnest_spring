package com.app.webnest.api.privateapi;

import com.app.webnest.domain.dto.*;
import com.app.webnest.domain.vo.QuizPersonalVO;
import com.app.webnest.domain.vo.QuizSubmitVO;
import com.app.webnest.domain.vo.QuizVO;
import com.app.webnest.exception.GlobalExceptionHandler;
import com.app.webnest.exception.QuizException;
import com.app.webnest.service.JavaCompileService;
import com.app.webnest.service.QuizService;
import com.app.webnest.service.UserService;
import com.app.webnest.util.JwtTokenUtil;
import com.sun.security.auth.UserPrincipal;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
public class    QuizApi {

    private final JwtTokenUtil jwtTokenUtil;
    private final JavaCompileService javaCompileService;
    private final QuizService quizService;
    private final UserService userService;


    @PostMapping("/quiz")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getQuizList(
            @RequestBody Map<String,Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        // 쿼리에 넘길 Map 구성
        if (params == null) params = new HashMap<>();
        String token = null;
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            token = authorizationHeader.substring(7);
        }

        String findUserEmail = (String) jwtTokenUtil.getUserEmailFromToken(token).get("userEmail");

        Long findUserId = userService.getUserIdByUserEmail(findUserEmail);
        Long userId = findUserId != null ? findUserId : null;

        String quizLanguage = params.get("quizLanguage") == null ? null : String.valueOf(params.get("quizLanguage"));
        String quizDifficult = params.get("quizDifficult") == null ? null : String.valueOf(params.get("quizDifficult"));
        String keyword = params.get("keyword") == null ? null : String.valueOf(params.get("keyword"));
        String quizPersonalIsSolve = params.get("quizPersonalIsSolve") == null ? null : String.valueOf(params.get("quizPersonalIsSolve"));


        int page = 1;
        int pageSize = 10; // 한 페이지에 보여줄 개수(필요시 params로 받을 수 있음)
        if (params.get("page") != null) {
            try {
                page = Integer.parseInt(String.valueOf(params.get("page")));
                if (page < 1) page = 1;
            } catch (NumberFormatException ignored) {}
        }

        int offset = (page - 1) * pageSize;


        HashMap<String, Object> filters = new HashMap<>();
        filters.put("quizLanguage", quizLanguage);
        filters.put("quizDifficult", quizDifficult);
        filters.put("keyword", keyword);
        filters.put("quizPersonalIsSolve", quizPersonalIsSolve);
        filters.put("offset", offset);
        filters.put("pageSize", pageSize);
        filters.put("userId", userId);
        List<QuizPersonalDTO> findQuizList = quizService.findQuizPersonal(filters);

        if (findQuizList == null) findQuizList = new ArrayList<>();

        Long quizTotalCount = quizService.quizCount(filters); // quizCount 쿼리는 TBL_QUIZ 기준으로 count 하도록 유지
        Map<String,Object> data = new HashMap<>();
        data.put("findQuizList", findQuizList);
        data.put("quizTotalCount", quizTotalCount);
        data.put("page", page);


        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("문제리스트 불러오기", data));
    };

    @PostMapping("/quiz/{quizId}/bookmark")
    public ResponseEntity<ApiResponseDTO<HashMap>> getQuizBookmarkAndIsSolve(
            @RequestBody QuizResponseDTO quizResponseDTO,
            @PathVariable("quizId") Long quizId
    ) {

        HashMap<String, Object> data = new HashMap<>();
        if(quizResponseDTO == null || quizResponseDTO.getUserId() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDTO.of("유저 아이디가 필요함", null));
        }
        quizResponseDTO.setQuizId(quizId);
        Long userId = quizResponseDTO.getUserId();

        QuizResponseDTO dto = new QuizResponseDTO();
        dto.setUserId(userId);
        dto.setQuizId(quizId);
//        북마크 눌렀을때 퍼스널테이블에 이미 해당유저가 있다면 북마크만 업데이트
        Long findPersonId = quizService.findQuizPersonalById(dto);
        if(findPersonId != null){
            quizService.isBookmarked(dto);
            List<QuizPersonalResponseDTO> existQuizPersonal = quizService.findByIsBookmarkIsSolve(userId);
            data.put("quizPersonal", existQuizPersonal);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("응답 성공", data));
        }
        log.info("findPersonId, {}",findPersonId);
            // 북마크를 눌렀을때 퍼스널테이블에 유저가없다면 새로생성하고 추가
        Long quizPersonalVO = quizService.findQuizPersonalById(dto);
        if (quizPersonalVO == null) {
            QuizPersonalVO newPersonalVO = new QuizPersonalVO();
            newPersonalVO.setUserId(userId);
            newPersonalVO.setQuizId(quizId);
            newPersonalVO.setQuizPersonalIsBookmark(1);
            quizService.saveQuizPersonal(newPersonalVO);
            log.info("SavedgetId: {}",newPersonalVO.getId());
        }

        List<QuizPersonalResponseDTO> findQuizPersonalInfo = quizService.findByIsBookmarkIsSolve(userId);
        log.info("findQuizPersonalInfo, {}",findQuizPersonalInfo);
        data.put("quizPersonal", findQuizPersonalInfo);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("응답 성공", data));
    }


    @PostMapping("/workspace/quiz/{id}")
    public ResponseEntity<ApiResponseDTO<HashMap>> getQuizById(@RequestBody QuizResponseDTO quizResponseDTO) {
        HashMap <String, Object> quizDatas = new HashMap<>();
        Long findQuizId = quizResponseDTO.getQuizId();
        Long findUserId = quizResponseDTO.getUserId();

        QuizVO findQuiz = quizService.findQuizById(findQuizId);
        log.info("findQuiz: {}", findQuiz);
        quizResponseDTO.setQuizId(findQuizId);
        quizResponseDTO.setUserId(findUserId);


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
        try {
        QuizResponseDTO dto = new QuizResponseDTO();
        HashMap<String, Object> submitDatas = new HashMap<>();
        Long findUserId = quizResponseDTO.getUserId();
        Long findQuizId = quizResponseDTO.getQuizId();
        String userCode = quizResponseDTO.getQuizSubmitCode();
        String userResultCode = quizResponseDTO.getQuizSubmitResultCode();

//        log.info("submitCode: {}", userResultCode);
//        log.info("finduserCode: {}", userCode);
//        log.info("findUserId: {}",findUserId);
//        log.info("findQuizId: {}",findQuizId);


        QuizPersonalVO findQuizPersonalVO = new QuizPersonalVO();
        findQuizPersonalVO.setUserId(findUserId);
        findQuizPersonalVO.setQuizId(findQuizId);

        QuizVO findQuiz = quizService.findQuizById(findQuizId);
        Integer findQuizExp = findQuiz.getQuizExp();

        dto.setUserId(findUserId);
        dto.setQuizId(findQuizId);
        dto.setQuizSubmitCode(userResultCode);



//        서브밋 테이블에 추가해야함
        quizService.saveQuizSubmit(dto);
        quizService.modifySubmitResult(dto);
//        퍼스널 테이블에 추가
        Long findPersonalId = quizService.findQuizPersonalById(dto);
        QuizPersonalVO findAllPersonalVO = quizService.findAllQuizPersonalById(findPersonalId);
        if(findPersonalId == null) {
            quizService.saveQuizPersonal(findQuizPersonalVO);
            quizService.isSolved(dto);
            userService.gainExp(findUserId, findQuizExp);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!",submitDatas));
        }
        if(findAllPersonalVO != null){
            quizService.isSolved(dto);
            userService.gainExp(findUserId, findQuizExp);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!",submitDatas));
        }

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!",submitDatas));
        } catch (Exception ex) {
            throw ex; // 다시 던져서 원래 동작 유지
        }
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
        Long findUserId = quizResponseDTO.getUserId();
        dto.setQuizId(quizId);
        dto.setUserId(findUserId);
        dto.setQuizSubmitCode(code);

        QuizPersonalVO findQuizPersonalVO = new QuizPersonalVO();
        findQuizPersonalVO.setUserId(findUserId);
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
        //        서브밋 테이블에 추가해야함
        quizService.saveQuizSubmit(dto);
        quizService.modifySubmitResult(dto);
//        퍼스널 테이블에 추가
        Long findPersonalId = quizService.findQuizPersonalById(dto);
        QuizPersonalVO findAllPersonalVO = quizService.findAllQuizPersonalById(findPersonalId);
        if(findPersonalId == null) {
            quizService.saveQuizPersonal(findQuizPersonalVO);
            quizService.isSolved(dto);
            userService.gainExp(findUserId, findQuizExp);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!",data));
        }
        if(findAllPersonalVO != null){
            quizService.isSolved(dto);
            userService.gainExp(findUserId, findQuizExp);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!",data));
        }

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

        QuizVO findQuiz = quizService.findQuizById(findQuizId);
        String quizExpectation = quizService.findQuizExpectationById(findQuizId).toUpperCase();
        Integer findQuizExp = findQuiz.getQuizExp();

        if(findCode == null) {
            throw new QuizException("소스코드를 다시 확인해주세요");
        }

        if(!findCode.equals(quizExpectation)) {
            throw new QuizException("잘못된 쿼리 ex) 소문자입력, 세미콜론 미작성");
        }

        quizService.saveQuizSubmit(dto);
        quizService.modifySubmitResult(dto);

        log.info("findUserId {}", findUserId);
        log.info("findQuizId {}", findQuizId);
        log.info("findCode: {}", findCode);
        Long findPersonalId = quizService.findQuizPersonalById(dto);
        QuizPersonalVO findAllPersonalVO = quizService.findAllQuizPersonalById(findPersonalId);
        if(findPersonalId == null) {
            quizService.saveQuizPersonal(findQuizPersonalVO);
            boolean isSolve = quizService.isSolved(dto);
            userService.gainExp(findUserId, findQuizExp);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!",data));
        }

        if(findAllPersonalVO != null){
            boolean isSolve = quizService.isSolved(dto);
            log.info("isSolve {}", isSolve);
            data.put("isSolved", isSolve);
            quizService.isSolved(dto);
            userService.gainExp(findUserId, findQuizExp);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!", data));
        }

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("기댓값과 일치합니다!\n다른 문제도 도전해보세요!!", data));
    }
}
