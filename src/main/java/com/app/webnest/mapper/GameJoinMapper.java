package com.app.webnest.mapper;

import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.vo.GameJoinVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GameJoinMapper {

    // 플레이어 전체 조회
    public List<GameJoinDTO> selectAll(Long gameRoomId);

    // 플레이어 게임 참여
    public void insert(GameJoinVO gameJoinVO);

    // 플레이어 게임 종료
    public void delete(GameJoinVO gameJoinVO);
}
