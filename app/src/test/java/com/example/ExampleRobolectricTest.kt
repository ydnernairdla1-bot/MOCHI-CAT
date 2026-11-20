package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CatMood
import com.example.model.DifficultyLevel
import com.example.model.FoodType
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MeongViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Meong Lapar`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Meong Lapar", appName)
    }

    @Test
    fun `feeding right food makes cat happy and wrong food makes cat angry`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MeongViewModel(app)

        viewModel.startGame(DifficultyLevel.HARD)
        assertEquals(AppScreen.GAME, viewModel.currentScreen.value)

        val initial = viewModel.gameState.value
        assertEquals(3, initial.lives)
        assertEquals(CatMood.HUNGRY, initial.mood)

        // Feed correct target food
        viewModel.feedCat(initial.targetFood)
        val afterHappy = viewModel.gameState.value
        assertEquals(CatMood.HAPPY, afterHappy.mood)
        assertTrue(afterHappy.score >= DifficultyLevel.HARD.basePoints)
        assertEquals(1, afterHappy.combo)

        // Feed a trap item (makes cat angry and deducts 1 life)
        viewModel.feedCat(FoodType.CHILI)
        val afterAngry = viewModel.gameState.value
        assertEquals(CatMood.ANGRY, afterAngry.mood)
        assertEquals(2, afterAngry.lives)
        assertEquals(0, afterAngry.combo)
    }
}
