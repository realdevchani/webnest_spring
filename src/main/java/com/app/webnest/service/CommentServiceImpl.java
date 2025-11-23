package com.app.webnest.service;

import com.app.webnest.domain.dto.CommentDTO;
import com.app.webnest.domain.dto.SubcommentDTO;
import com.app.webnest.domain.vo.CommentVO;
import com.app.webnest.domain.vo.PostVO;
import com.app.webnest.repository.CommentDAO;
import com.app.webnest.repository.CommentLikeDAO;
import com.app.webnest.repository.SubcommentDAO;
import com.app.webnest.repository.SubcommentLikeDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentDAO commentDAO;
    private final SubcommentDAO subcommentDAO;
    private final CommentLikeDAO commentLikeDAO;
    private final SubcommentLikeDAO subcommentLikeDAO;

    @Override
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        return commentDAO.findCommentPostId(postId);
    }
    
    @Override
    public CommentDTO getCommentById(Long commentId) {
        return commentDAO.findCommentById(commentId);
    }




    //답글 작성
    @Override
    public Map<String, Long> writeComment(CommentVO commentVO) {
        Map<String, Long> response = new HashMap<>();
        Long newCommentId = commentDAO.saveComment(commentVO);
        response.put("newCommentId", newCommentId);
        return response;
    }

    //답글 수정
    @Override
    public void modifyComment(CommentVO commentVO) {
        commentDAO.modifyComment(commentVO);
    }

    //답글 삭제
    @Override
    public void removeComment(Long id) {
        // 댓글 삭제 전에 관련된 데이터들을 먼저 삭제
        // 1. 해당 댓글의 대댓글들 조회
        List<SubcommentDTO> subcomments = subcommentDAO.findAll(id);
        // 2. 각 대댓글의 좋아요들 삭제
        for (SubcommentDTO subcomment : subcomments) {
            subcommentLikeDAO.removeBySubcommentId(subcomment.getId());
        }
        // 3. 대댓글들 삭제
        subcommentDAO.removeSubcommentsByCommentId(id);
        // 4. 해당 댓글의 좋아요들 삭제
        commentLikeDAO.removeByCommentId(id);
        // 5. 그 다음 댓글 삭제
        commentDAO.delete(id);
    }

    //채택
    @Override
    public void chooseComment(Long commentId) {
        commentDAO.choose(commentId);
    }


}

