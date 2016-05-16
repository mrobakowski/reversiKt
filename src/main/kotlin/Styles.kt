import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import tornadofx.*

class Styles: Stylesheet() {
    companion object {
        val cell by cssclass()
        val whiteDisk by cssclass()
        val blackDisk by cssclass()
        val legalCell by cssclass()
        val btn by cssclass()
        val btnContainer by cssclass()
    }

    init {
        s(cell) {
            backgroundColor = Color.GREEN
            borderColor = box(Color.BLACK)
            borderWidth = box(1.px)
            borderStyle = BorderStrokeStyle.SOLID
        }

        s(legalCell) {
            backgroundColor = Color.LIGHTGREEN
        }

        val disk = mixin {
            backgroundRadius = box(100.percent)
            backgroundInsets = box(5.px)
        }

        s(whiteDisk) {
            mix(disk)
            backgroundColor = Color.WHITE
        }

        s(blackDisk) {
            mix(disk)
            backgroundColor = Color.BLACK
        }

        s(btn) {
            endMargin = 5.px
            startMargin = 5.px
        }

        s(btnContainer) {
            padding = box(5.px)
        }
    }
}