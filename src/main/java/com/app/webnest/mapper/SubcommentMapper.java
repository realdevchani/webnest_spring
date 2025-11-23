package com.app.webnest.mapper;

import com.app.webnest.domain.dto.SubcommentDTO;
import com.app.webnest.domain.vo.SubcommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubcommentMapper {
    List<SubcommentDTO> selectSubcomment(Long commentId);
    
    // 대댓글 작성
    void insert(SubcommentVO subcommentVO);
    
    // 대댓글 삭제
    void delete(Long id);
    
    // 댓글 ID로 대댓글들 삭제
    void deleteByCommentId(Long commentId);
}
//// 게시글 상세조회
//public Optional<PostDTO> selectOne(Long id);
//
//// 게시글의 댓글 조회
//public List<CommentDTO> selectComment(Long postTestId);
//
//// 게시글의 대댓글 조회
//public List<ReplyDTO> selectReply(Long commentTestId);
