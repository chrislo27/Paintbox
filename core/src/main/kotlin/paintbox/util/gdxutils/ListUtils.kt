package paintbox.util.gdxutils

import java.util.*
import java.util.concurrent.ThreadLocalRandom


fun <T> List<T>.random(random: Random = ThreadLocalRandom.current()): T =
    this[random.nextInt(this.size)]
