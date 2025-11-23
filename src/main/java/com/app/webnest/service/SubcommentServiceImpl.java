package com.app.webnest.service;

import com.app.webnest.domain.dto.SubcommentDTO;
import com.app.webnest.domain.vo.SubcommentVO;
import com.app.webnest.repository.SubcommentDAO;
import com.app.webnest.repository.SubcommentLikeDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SubcommentServiceImpl implements SubcommentService {
    private final SubcommentDAO subcommentDAO;
    private final SubcommentLikeDAO subcommentLikeDAO;

    @Override
    public List<SubcommentDTO> getSubcomments(Long commentId) {
        return  subcommentDAO.findAll(commentId);
    }

    // 대댓글 작성
    @Override
    public Map<String, Long> writeSubcomment(SubcommentVO subcommentVO) {
        Map<String, Long> response = new HashMap<>();
        Long newSubcommentId = subcommentDAO.saveSubcomment(subcommentVO);
        response.put("newSubcommentId", newSubcommentId);
        return response;
    }

    // 대댓글 삭제
    @Override
    public void removeSubcomment(Long id) {
        // 대댓글 삭제 전에 해당 대댓글의 좋아요들을 먼저 삭제
        subcommentLikeDAO.removeBySubcommentId(id);
        // 그 다음 대댓글 삭제
        subcommentDAO.removeSubcomment(id);
    }
}
//public List<SubcommentDTO> findAll(Long commentId) {return subcommentMapper.selectSubcomment(commentId);}