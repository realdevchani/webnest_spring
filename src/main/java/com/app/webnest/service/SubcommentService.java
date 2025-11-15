package com.app.webnest.service;



import com.app.webnest.domain.dto.SubcommentDTO;
import com.app.webnest.domain.vo.SubcommentVO;

import java.util.List;
import java.util.Map;

public interface SubcommentService {
    // 게시글 목록
    public List<SubcommentDTO> getSubcomments(Long commentId);
    
    // 대댓글 작성
    public Map<String, Long> writeSubcomment(SubcommentVO subcommentVO);
    
    // 대댓글 삭제
    public void removeSubcomment(Long id);
}
//public List<SubcommentDTO> findAll(Long commentId) {return subcommentMapper.selectSubcomment(commentId);}