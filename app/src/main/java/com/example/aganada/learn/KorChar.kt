package com.example.aganada.learn

import kr.bydelta.koala.*

class KorChar {
    fun check(): Boolean {
        println('가'.dissembleHangul())
        return true
    }
    /*
    val beginCode = 0xAC00  // 시작 유니코드 (44032)
    val endCode = 0xD7AF  // 끝 유니코드 (55215)

    // 초성, 중성, 종성
    val onset = arrayOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
        'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
    val nucleus = arrayOf('ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
    'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ')
    val coda = arrayOf(Character.MIN_VALUE, 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ',
    'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ',
    'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')

    // 초성, 중성, 종성 획수
    val onsetStrokes = arrayOf(1, 2, 1, 2, 4, 3, 3, 4, 8, 2, 4, 1, 2, 4, 3, 2, 3, 4, 3)
    val nucleusStrokes = arrayOf(2, 3, 3, 4, 2, 3, 3, 4, 2, 4, 5, 3, 3, 2, 4, 5, 3, 3, 1, 2, 1)
    val codaStrokes = arrayOf(0, 1, 2, 3, 1, 3, 4, 2, 3, 4, 6, 7, 5, 6, 7, 6, 3, 4, 6, 2, 4, 1, 2, 3, 2, 3, 4, 3)

    // 쉬프트 키 사용 문자열
    val shiftedCharSet = """~!@#$%^&*()_+{}|:"<>?"""

    private fun splitSyllable(): {

    }
    */

    /*
    def splitsyllable(self):
    """초/중/종성이 분리된 문자열을 반환합니다.
        인스턴스 문자가 (분리 가능한) 완전한 한글 음절이 아닌 경우, 인스턴스
        문자를 그대로 반환합니다.
        Returns
        -------
        tuple[bool,str]
            (분리 가능 여부, 초/중/종성이 분리된 문자열).
            반환 요소의 첫번째는 분리 가능 여부를 나타내는 부울 값입니다.
        """
    # 분리가 가능한 한글 음절이 아닌 경우 인스턴스 문자열을 그대로 반환
    if not self.issyllable():
    return False, self._char
    # 한글 음절인 경우 변환 수행
    foo = ord(self._char) - KorChar._BEGIN_CODE
    onsetidx = foo // 588
    nucleusidx = (foo - onsetidx * 588) // 28
    codaidx = foo - onsetidx * 588 - 28 * nucleusidx
    jamo = KorChar._ONSET[onsetidx] + KorChar._NUCLEUS[nucleusidx] + KorChar._CODA[codaidx]
    return True, jamo
     */
}