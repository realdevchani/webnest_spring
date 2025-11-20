package com.app.webnest.repository;

import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.dto.GameRoomDTO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.mapper.GameJoinMapper;
import com.app.webnest.mapper.GameRoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GameJoinDAO {

    private final GameJoinMapper gameJoinMapper;

    // 플레이어 전체조회
    public List<GameJoinDTO> getPlayers(Long gameRoomId) {
        return gameJoinMapper.selectAll(gameRoomId);
    }

    // 플레이어 게임 참여
    public void save(GameJoinVO gameJoinVO){
        gameJoinMapper.insert(gameJoinVO);
    }

    // 플레이어 게임 종료
    public void delete(GameJoinVO gameJoinVO){
        gameJoinMapper.delete(gameJoinVO);
    }
    
    // 게임방의 모든 플레이어 삭제
    public void deleteAllByGameRoomId(Long gameRoomId){
        gameJoinMapper.deleteAllByGameRoomId(gameRoomId);
    }

    // 플레이어 팀 컬러 업데이트
    public void updateTeamColor(GameJoinVO gameJoinVO){
        gameJoinMapper.updateTeamColor(gameJoinVO);
    }

    // 일반 업데이트 (원격에서 추가된 메서드)
    public void update(GameJoinVO gameJoinVO){
        gameJoinMapper.update(gameJoinVO);
    }

    // 현재 방에 있는 유저 리스트를 가져온다.
    public List<GameJoinDTO> findUserListByGameRoomId(Long gameRoomId) {
        return gameJoinMapper.selectUserListByGameRoomId(gameRoomId);
    }
    
    // 유저의 정보 가져온다. 1. 위치 정보, 2. 턴 정보
    public Boolean findUserTurn(GameJoinVO gameJoinVO) {
        return gameJoinMapper.selectUserTurn(gameJoinVO);
    }
    public Integer findUserPosition(GameJoinVO gameJoinVO) {
        return gameJoinMapper.selectUserPosition(gameJoinVO);
    }
    
    // 선택한 유저의 정보를 수정한다. 위치 이동, 턴 변경
    public void modifyUserTurn(GameJoinVO gameJoinVO){
        gameJoinMapper.updateUserTurn(gameJoinVO);
    }
    public void modifyUserPosition(GameJoinVO gameJoinVO){
        gameJoinMapper.updateUserPosition(gameJoinVO);
    }
    public void modifyCurrentUserTurn(GameJoinVO gameJoinVO){ gameJoinMapper.updateCurrentUserTurn(gameJoinVO); }
    
    // 게임이 끝났을 때는 게임방에 있는 모두를 변경해줘야 한다.
        public void modifyAllTurn(Long gameRoomId){
            gameJoinMapper.updateAllTurn(gameRoomId);
        }

        // 게임 종료 시 모든 플레이어 포지션 초기화
        public void resetAllPosition(Long gameRoomId) {
            gameJoinMapper.resetAllPosition(gameRoomId);
        }

        // 게임 종료 시 모든 플레이어 레디 상태 초기화
        public void resetAllReady(Long gameRoomId) {
            gameJoinMapper.resetAllReady(gameRoomId);
        }

        // 준비 상태 업데이트
        public void updateReady(GameJoinVO gameJoinVO) {
            gameJoinMapper.updateReady(gameJoinVO);
        }

    public Optional<GameJoinVO> findUserInGameRoom(GameJoinVO gameJoinVO) {
        return gameJoinMapper.selectGameUserByUserIdAndGameRoom(gameJoinVO);
    }

    public List<GameJoinVO> findPlayerListByEntrancedTime(Long gameRoomId) {
        return gameJoinMapper.selectUserListOrderedEntrancedTime(gameRoomId);
    }
}
