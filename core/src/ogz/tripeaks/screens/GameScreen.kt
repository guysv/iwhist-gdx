package ogz.tripeaks.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable
import ktx.app.KtxScreen
import ktx.graphics.use
import ktx.scene2d.Scene2DSkin
import ogz.tripeaks.*

class GameScreen(val game: Game) : KtxScreen {
    private val camera = OrthographicCamera()
    private val viewport = IntegerScalingViewport(Constants.CONTENT_WIDTH.toInt(), Constants.CONTENT_HEIGHT.toInt(), camera)
    private val touchPoint3D = Vector3()
    private val touchPoint2D = Vector2()
    private val state = GameState(game.assets)
    private val stage = Stage(viewport)
    private var paused = false

    private val dealButton by lazy {
        Button(Scene2DSkin.defaultSkin).apply {
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    state.deal()
                }
            })
            setSize(Constants.SPRITE_WIDTH, Constants.SPRITE_HEIGHT)
            setPosition(Constants.STACK_POSITION.x + Constants.CELL_WIDTH * 2f + 1f, Constants.STACK_POSITION.y)
            children.add(
                    Image(SpriteDrawable(game.assets[TextureAtlasAssets.Ui].createSprite("deal"))).apply {
                        setPosition(Constants.SPRITE_WIDTH / 2f - 8f, Constants.SPRITE_HEIGHT / 2f - 8f)
                        touchable = Touchable.disabled
                    }
            )
        }
    }

    private val undoButton by lazy {
        Button(Scene2DSkin.defaultSkin).apply {
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    state.undo()
                }
            })
            setSize(Constants.SPRITE_WIDTH, Constants.SPRITE_HEIGHT)
            setPosition(Constants.DISCARD_POSITION.x - Constants.CELL_WIDTH * 2f, Constants.DISCARD_POSITION.y)
            children.add(
                    Image(SpriteDrawable(game.assets[TextureAtlasAssets.Ui].createSprite("undo"))).apply {
                        setPosition(Constants.SPRITE_WIDTH / 2f - 8f, Constants.SPRITE_HEIGHT / 2f - 8f)
                        touchable = Touchable.disabled
                    }
            )
        }
    }

    override fun render(delta: Float) {
        camera.update()
        state.update(delta)

        if (state.won && !paused) {
            showNewGameDialog("You won!", "New game", "Exit") {
                Gdx.app.exit()
            }
        }

        stage.act(delta)

        dealButton.isDisabled = !state.canDeal || state.won
        undoButton.isDisabled = !state.canUndo || state.won
        dealButton.touchable = if (dealButton.isDisabled) Touchable.disabled else Touchable.enabled
        undoButton.touchable = if (undoButton.isDisabled) Touchable.disabled else Touchable.enabled

        Gdx.gl.glClearColor(0.39f, 0.64f, 0.28f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        game.batch.enableBlending()
        game.batch.use(camera) { batch ->
            state.draw(batch)
        }
        game.batch.disableBlending()

        stage.draw()

        if (Gdx.input.justTouched() && !paused) {
            touchPoint3D.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            viewport.unproject(touchPoint3D)
            touchPoint2D.set(touchPoint3D.x, touchPoint3D.y)
            state.touch(touchPoint2D)
        }
    }

    override fun show() {
        if (!load()) {
            state.init()
        }

        stage.actors.addAll(dealButton, undoButton)

        // The new game button
        stage.actors.add(Button(Scene2DSkin.defaultSkin).apply {
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    showNewGameDialog("Start a new game?", "Start", "Cancel")
                }
            })
            setSize(Constants.SPRITE_WIDTH, Constants.SPRITE_WIDTH)
            setPosition(
                    Constants.CONTENT_WIDTH - Constants.SPRITE_WIDTH - 2f,
                    Constants.CONTENT_HEIGHT - Constants.SPRITE_WIDTH - Constants.VERTICAL_PADDING - 3f
            )
            children.add(
                    Image(SpriteDrawable(game.assets[TextureAtlasAssets.Ui].createSprite("new"))).apply {
                        setPosition(Constants.SPRITE_WIDTH / 2f - 7f, Constants.SPRITE_WIDTH / 2f - 7f)
                        touchable = Touchable.disabled
                    }
            )
        })

        Gdx.input.inputProcessor = stage
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun dispose() {
        if (!state.won) {
            save()
        }
        super.dispose()
    }

    override fun pause() {
        if (!state.won) {
            save()
        } else {
            Gdx.app.getPreferences(Constants.PREFERENCES_SAVE_KEY)
                    .putBoolean(Constants.PREFERENCES_VALID_KEY, false)
        }
        super.pause()
    }

    override fun resume() {
        load()
        super.resume()
    }

    private fun showNewGameDialog(text: String, okText: String, cancelText: String, onCancel: () -> Unit = {}) {
        val dialog = object : Dialog("", Scene2DSkin.defaultSkin) {
            override fun result(obj: Any?) {
                paused = false
                if (obj == true) {
                    state.init()
                } else {
                    onCancel()
                }
            }
        }.apply {
            buttonTable.pad(8f, 0f, 0f, 0f)
            buttonTable.defaults().width(60f)
            pad(16f, 24f, 16f, 24f)
            text(text)
            button(okText, true)
            button(cancelText, false)
            key(Input.Keys.ENTER, true)
            key(Input.Keys.ESCAPE, false)
        }
        paused = true
        dialog.show(stage)
    }

    private fun save() {
        val preferences = Gdx.app.getPreferences(Constants.PREFERENCES_SAVE_KEY)
        preferences.putBoolean(Constants.PREFERENCES_VALID_KEY, true)
        state.save(preferences)
        preferences.flush()
    }

    private fun load(): Boolean {
        val preferences = Gdx.app.getPreferences(Constants.PREFERENCES_SAVE_KEY)
        if (preferences.getBoolean(Constants.PREFERENCES_VALID_KEY, false)) {
            return try {
                state.load(preferences)
                preferences.putBoolean(Constants.PREFERENCES_VALID_KEY, false)
                preferences.flush()
                true
            } catch(_: Exception){
                false
            }
        }
        return false
    }
}