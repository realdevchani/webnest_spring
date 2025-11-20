package com.app.webnest.service;

import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.vo.GameJoinVO;

import java.util.List;
import java.util.Optional;

public interface GameJoinService {

    // 게임방 참여
    public void join(GameJoinVO gameJoinVO);

    // 게임방 종료
    public void leave(GameJoinVO gameJoinVO);

    // 플레이어 팀 컬러 업데이트
    public void updateTeamColor(GameJoinVO gameJoinVO);

    //    현재 방에 있는 유저 리스트를 가져온다.-->
    public List<GameJoinDTO> getArrangeUserByTurn(Long gameRoomId);
    //<!--    유저의 정보 가져온다. 1. 위치 정보, 2. 턴 정보-->
    public Integer getUserPosition(GameJoinVO gameJoinVO);
    public boolean getUserTurn(GameJoinVO gameJoinVO);
    //            <!--    선택한 유저의 정보를 수정한다. 위치 이동, 턴 변경-->
    public void updateUserPosition(GameJoinVO gameJoinVO);
    public void updateUserTurn(GameJoinVO gameJoinVO);
    public void updateCurrentUserTurn(GameJoinVO gameJoinVO);
    //<!--    게임이 끝났을 때는 게임방에 있는 모두를 변경해줘야 한다.
    public void updateAllUserTurn(Long gameRoomId);
    
    // 게임 종료 시 모든 플레이어 포지션 초기화
    public void resetAllPosition(Long gameRoomId);
    
    // 게임 종료 시 모든 플레이어 레디 상태 초기화
    public void resetAllReady(Long gameRoomId);
    
    // 준비 상태 업데이트
    public void updateReady(GameJoinVO gameJoinVO);
    
    // 게임방 플레이어 전체 조회
    public List<GameJoinDTO> getPlayers(Long gameRoomId);

    public void update(GameJoinVO gameJoinVO);

    public Optional<GameJoinVO> getGameJoinDTOByGameRoomId(GameJoinVO gameJoinVO);

    public List<GameJoinVO> getUserListByEntrancedTime(Long gameRoomId);
}
