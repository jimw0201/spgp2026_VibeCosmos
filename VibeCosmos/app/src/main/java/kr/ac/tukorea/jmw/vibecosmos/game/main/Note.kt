package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.graphics.RectF
import kr.ac.tukorea.jmw.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.jmw.a2dg.objects.IRecyclable
import kr.ac.tukorea.jmw.a2dg.objects.Sprite
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.R

class Note(gctx: GameContext) : Sprite(gctx, R.mipmap.air1), IRecyclable, IBoxCollidable {
    // 해당 노트가 생성된 레인
    var lane: Player.State = Player.State.RUN

    // 충돌 감지를 위해 현재 그려지는 영역 반환
    override val collisionRect: RectF get() = dstRect

    override fun onRecycle() {
        // 객체 풀에 반환될 때 필요한 초기화 로직
    }

    // 적 노트 기본 크기 100x100으로 초기화
    init {
        width = 100f
        height = 100f
    }

    // 노트 재사용 시 상태 초기화하는 함수
    fun reset(lane: Player.State) {
        this.lane = lane
        // 레인 종류에 따라 Y축 위치 겨ㅕㅕㄹ정
        val targetY = if (lane == Player.State.UP_ATK) 300f else 500f

        // 화면 오른쪽 끝에서 시작하도록 설정
        setCenter(1700f, targetY)
        syncDstRect()
    }

    // 매 프레임마다 노트를 왼쪽으로 이동
    override fun update(gctx: GameContext) {
        val elapsedSeconds = gctx.frameTime
        // 초당 600픽셀
        x -= 600 * elapsedSeconds
        syncDstRect()
    }
}