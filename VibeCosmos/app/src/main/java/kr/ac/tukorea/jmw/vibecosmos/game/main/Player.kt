package kr.ac.tukorea.jmw.vibecosmos.game.main

import android.graphics.Canvas
import android.graphics.RectF
import kr.ac.tukorea.jmw.a2dg.objects.AnimSprite
import kr.ac.tukorea.jmw.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.jmw.a2dg.view.GameContext
import kr.ac.tukorea.jmw.vibecosmos.R

class Player(gctx: GameContext): AnimSprite(
    gctx,
    R.mipmap.player_run, // 기본 상태
    60f, // 기본 애니메이션 FPS
    frameCount = 32, // 전체 프레임 수
    columns = 8 // 스프라이트 시트의 가로 열 수
    ), IBoxCollidable {

    private val fixedCollisionRect = RectF()

    override val collisionRect: RectF
        get() {
            fixedCollisionRect.set(
                x - WIDTH / 2,
                y - HEIGHT / 2,
                x + WIDTH / 2,
                y + HEIGHT / 2
            )
            return fixedCollisionRect
        }

    // 플레이어 행동 상태 정의
    enum class State {
        RUN, UP_ATK, DOWN_ATK,
    }

    // 플레이어의 체력
    var hp = 200

    // 특정 상태가 시작된 시간을 기록하여 애니메이션 종료 시점을 파악
    private var stateStartTime = System.currentTimeMillis()

    init {
        // 초기 크기 설정
        width = Player.WIDTH
        height = Player.HEIGHT

        // 초기 위치 설정
        setCenter(200f, 500f)
    }

    // 플레이어의 현재 상태를 관리하는 프로퍼티
    var state = State.RUN
        set(value) {
            if (field == value && value == State.RUN) return

            field = value

            // 상태에 따라 변경될 설정값 배열 (이미지 ID, 총 프레임 수, 크기 배율, FPS, 이동할 Y좌표)
            val (resId, frameCount, scale, targetFps, targetY) = when (value) {
                State.RUN -> arrayOf(R.mipmap.player_run, 32, 1.0f, 60f, 500f)
                State.UP_ATK -> arrayOf(R.mipmap.player_up_atk, 30, 1.3f, 90f, 300f)
                State.DOWN_ATK -> arrayOf(R.mipmap.player_down_atk, 22, 1.3f, 90f, 500f)
            }

            // 상태에 맞는 스프라이트 이미지 및 애니메이션 정보 교체
            bitmap = gctx.res.getBitmap(resId as Int)
            this.frameCount = frameCount as Int
            this.fps = targetFps as Float

            // 공격 상태일 때 지정된 배율만큼 크기 키우고, 타겟 레인으로 위치 이동
            this.width = WIDTH * (scale as Float)
            this.height = HEIGHT * scale
            setCenter(200f, targetY as Float)

            // 상태 변경 시간을 갱신하여 애니메이션 재생 시간을 0부터 다시 시작
            this.stateStartTime = System.currentTimeMillis()
        }

    override fun draw(canvas: Canvas) {
        syncDstRect()

        // 현재 상태가 시작된 후 경과한 시간 계산
        val elapsedSeconds = (System.currentTimeMillis() - stateStartTime) / 1000f

        // 현재 애니메이션의 1회 전체 재생에 걸리는 총 시간
        val totalDuration = frameCount / fps

        // 달리기 상태가 아닌데 애니메이션 1회 재생이 끝났다면 다시 RUN 상태로 복귀
        if (state != State.RUN && elapsedSeconds >= totalDuration) {
            state = State.RUN
        }

        // 현재 경과 시간에 따른 재생 프레임 인덱스 계산
        val frameIndex = ((elapsedSeconds * fps).toInt()) % frameCount

        // 프레임 인덱스를 바탕으로 스프라이트 시트 내의 잘라낼 행, 열 계산
        val col = frameIndex % columns
        val row = frameIndex / columns

        // 워논 이미지에서 현재 프레임 영역을 지정하여 화면에 그림
        srcRect?.set(
            col * frameWidth,
            row * frameHeight,
            (col + 1) * frameWidth,
            (row + 1) * frameHeight
        )
        canvas.drawBitmap(bitmap, srcRect, dstRect, null)
    }

    // 상단 공격 함수
    fun attackUp() {
        state = State.UP_ATK
    }

    // 하단 공격 함수
    fun attackDown() {
        state = State.DOWN_ATK
    }

    companion object {
        // 플레이어 스프라이트의 기본 원본 가로, 세로 크기 상수
        const val WIDTH = 200f
        const val HEIGHT = 261f
    }
}