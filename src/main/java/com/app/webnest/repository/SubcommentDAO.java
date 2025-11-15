package com.app.webnest.repository;

import com.app.webnest.domain.dto.SubcommentDTO;
import com.app.webnest.domain.vo.SubcommentVO;
import com.app.webnest.mapper.SubcommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SubcommentDAO {
    private final SubcommentMapper subcommentMapper;

    public List<SubcommentDTO> findAll(Long commentId) {return subcommentMapper.selectSubcomment(commentId);}

    // 대댓글 작성
    public Long saveSubcomment(SubcommentVO subcommentVO) {
        subcommentMapper.insert(subcommentVO);
        return subcommentVO.getId();
    }

    // 대댓글 삭제
    public void removeSubcomment(Long id) {
        subcommentMapper.delete(id);
    }
}
