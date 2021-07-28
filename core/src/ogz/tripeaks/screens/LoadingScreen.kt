package ogz.tripeaks.screens

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxScreen
import ktx.scene2d.Scene2DSkin
import ktx.style.*
import ogz.tripeaks.*
import ogz.tripeaks.util.GamePreferences
import ogz.tripeaks.util.SkinData

class LoadingScreen(
    private val game: Game,
    private val batch: Batch,
    private val viewport: Viewport,
    private val assets: AssetManager,
    private val preferences: GamePreferences
) :
    KtxScreen {

    override fun show() {
        for (asset in TextureAtlasAssets.values()) assets.load(asset)
        for (asset in TextureAssets.values()) assets.load(asset)
        for (asset in FontAssets.values()) assets.load(asset)
        for (asset in BundleAssets.values()) {
            assets.load(asset)
        }
    }

    override fun render(delta: Float) {
        assets.update()
        viewport.apply()

        if (assets.isFinished) {
            val skinData = buildSkin(assets[BundleAssets.Bundle].get("skinKey"))
            Scene2DSkin.defaultSkin = skinData.skin
            game.context.bindSingleton(skinData)
            game.addScreen(StartScreen(game, assets, viewport, batch, skinData, preferences))
            game.setScreen<StartScreen>()
            game.removeScreen<LoadingScreen>()
            dispose()
        }
    }

    private fun buildSkin(key: String): SkinData {
        if (key == "cjk") {
            val skin = makeCommonSkin(assets[FontAssets.UnifontCjk16])
            return SkinData(skin, -1f, 4f, 7f, 4f)
        } else {
            val skin = makeCommonSkin(assets[FontAssets.GameFont])
            return SkinData(skin, 3f, 4f, 19f, 2f)
        }
    }

    private fun makeCommonSkin(skinFont: BitmapFont): Skin =
        skin(assets[TextureAtlasAssets.Ui]) { skin ->
            color("light", 242f / 255f, 204f / 255f, 143f / 255f, 1f)
            color("dark", 76f / 255f, 56f / 255f, 77f / 255f, 1f)
            label {
                font = skinFont
                fontColor = skin["dark"]
            }
            label("light", extend = defaultStyle)
            label("dark", extend = defaultStyle) {
                fontColor = skin["light"]
            }
            button {
                up = skin["buttonUp"]
                down = skin["buttonDown"]
                disabled = skin["buttonDisabled"]
                pressedOffsetY = -1f
            }
            button("light", extend = defaultStyle)
            button("dark", extend = defaultStyle) {
                up = skin["buttonUp_dark"]
                down = skin["buttonDown_dark"]
                disabled = skin["buttonDisabled_dark"]
            }
            textButton {
                up = skin["buttonUp"]
                down = skin["buttonDown"]
                disabled = skin["buttonDisabled"]
                font = skinFont
                fontColor = skin["dark"]
                pressedOffsetY = -1f
            }
            textButton("light", extend = defaultStyle)
            textButton("dark", extend = defaultStyle) {
                up = skin["buttonUp_dark"]
                down = skin["buttonDown_dark"]
                disabled = skin["buttonDisabled_dark"]
                fontColor = skin["light"]
            }
            window {
                titleFont = skinFont
                titleFontColor = skin["dark"]
                background = skin["window"]
            }
            window("light", extend = defaultStyle)
            window("dark", extend = defaultStyle) {
                titleFontColor = skin["light"]
                background = skin["window_dark"]
            }
            checkBox {
                checkboxOn = skin["checkboxOnLight"]
                checkboxOff = skin["checkboxOffLight"]
                font = skinFont
                fontColor = skin["dark"]
            }
            checkBox("light", extend = defaultStyle)
            checkBox(name = "dark", extend = defaultStyle) {
                checkboxOn = skin["checkboxOn_dark"]
                checkboxOff = skin["checkboxOff_dark"]
                fontColor = skin["light"]
            }
        }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}
