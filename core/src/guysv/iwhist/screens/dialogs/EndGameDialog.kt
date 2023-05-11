package guysv.iwhist.screens.dialogs

import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import guysv.iwhist.Const
import guysv.iwhist.screens.controls.MyTextButton
import guysv.iwhist.util.SkinData

class EndGameDialog(
    skinData: SkinData,
    theme: String,
    removedFromStack: Int,
    longestChain: Int,
    usedUndo: Int,
    val res: I18NBundle,
) : Dialog("", skinData.skin, theme) {

    val newGameButton = MyTextButton(res.get("newGame"), skinData, theme)
    val exitButton = MyTextButton(res.get("exit"), skinData, theme)

    init {
        val removedLabel = Label(res.format("fromStack", removedFromStack), skinData.skin, theme)
        val undoLabel = Label(res.format("usedUndo", usedUndo), skinData.skin, theme)
        val chainLabel = Label(res.format("longestChain", longestChain), skinData.skin, theme)
        pad(16f, 24f, 16f, 24f)
        contentTable.apply {
            add(Label(res.get("won"), skinData.skin, theme)).height(16f)
            row()
            add(removedLabel).align(Align.left)
            row()
            add(undoLabel).align(Align.left)
            row()
            add(chainLabel).align(Align.left)
        }
        buttonTable.apply {
            pad(4f, 4f, 0f, 4f)
            defaults().width(108f).space(4f).height(Const.BUTTON_HEIGHT).pad(0f)
            add(newGameButton)
            add(exitButton)
        }
    }
}
