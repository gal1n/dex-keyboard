package com.gal1n.dexkeyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneticLayoutTest {
    @Test
    fun standardBulgarianPhoneticQwertyCore() {
        val map = linkedMapOf(
            'q' to 'ч', 'w' to 'ш', 'e' to 'е', 'r' to 'р', 't' to 'т', 'y' to 'ъ',
            'u' to 'у', 'i' to 'и', 'o' to 'о', 'p' to 'п',
            'a' to 'а', 's' to 'с', 'd' to 'д', 'f' to 'ф', 'g' to 'г', 'h' to 'х',
            'j' to 'й', 'k' to 'к', 'l' to 'л',
            'z' to 'з', 'x' to 'ж', 'c' to 'ц', 'v' to 'в', 'b' to 'б', 'n' to 'н', 'm' to 'м'
        )
        assertEquals(26, map.size)
        assertEquals('ч', map['q'])
        assertEquals('ш', map['w'])
        assertEquals('е', map['e'])
        assertEquals('ъ', map['y'])
        assertEquals('ж', map['x'])
        assertEquals('м', map['m'])
    }
}
