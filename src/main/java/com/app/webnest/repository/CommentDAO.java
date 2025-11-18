package com.app.webnest.repository;

import com.app.webnest.domain.dto.CommentDTO;
import com.app.webnest.domain.dto.PostResponseDTO;
import com.app.webnest.domain.vo.CommentVO;
import com.app.webnest.domain.vo.PostVO;
import com.app.webnest.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class CommentDAO {
    private final CommentMapper commentMapper;

    public List<CommentDTO> findCommentPostId(Long postId) {
        return commentMapper.selectByPostId(postId);
    }
    
    // 댓글 ID로 댓글 조회
    public CommentDTO findCommentById(Long commentId) {
        return commentMapper.selectById(commentId);
    }



    //답글 작성
    public Long saveComment(CommentVO commentVO) {
        commentMapper .insertComment(commentVO);
        return commentVO.getId();
    }

    //답글 수정
    public void modifyComment(CommentVO commentVO) {
        commentMapper.updateComment(commentVO);
    }

    //답글 삭제
    public void delete(Long id){
        commentMapper.deleteComment(id);
    }

    //채택
    public void choose(Long commentId) {
        commentMapper.acceptComment(commentId);
    }
}
