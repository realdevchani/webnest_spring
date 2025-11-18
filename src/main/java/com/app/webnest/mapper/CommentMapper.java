package com.app.webnest.mapper;

import com.app.webnest.domain.dto.CommentDTO;
import com.app.webnest.domain.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 댓글 가지고 오기
    List<CommentDTO> selectByPostId(Long id);
    
    // 댓글 ID로 댓글 조회
    CommentDTO selectById(Long id);

    //답글 작성
    public Long insertComment(CommentVO commentVO);

    //답글 수정
    public void updateComment(CommentVO commentVO);

    //답글 삭제
    public void deleteComment(Long id);

    //채택
    public void acceptComment(Long commentId);

}
