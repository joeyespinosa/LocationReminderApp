package com.udacity.locationreminderapp

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.locationreminderapp.locationreminders.RemindersActivity
import com.udacity.locationreminderapp.locationreminders.data.ReminderDataSource
import com.udacity.locationreminderapp.locationreminders.data.local.LocalDB
import com.udacity.locationreminderapp.locationreminders.data.local.RemindersLocalRepository
import com.udacity.locationreminderapp.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.locationreminderapp.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.locationreminderapp.util.DataBindingIdlingResource
import com.udacity.locationreminderapp.util.monitorActivity
import com.udacity.locationreminderapp.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }

        startKoin {
            modules(listOf(myModule))
        }

        repository = get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


//    TODO: add End to End testing to the app
    @Test
    fun createReminder() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("Reminder"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("Description"), ViewActions.closeSoftKeyboard())

        onView(withId(R.id.selectLocation)).perform(click())

        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withText("Reminder")).check(matches(isDisplayed()))
        onView(withText("Description")).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun shouldReturnError_noTitle() = runBlocking{
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("Description"), ViewActions.closeSoftKeyboard())

        onView(withId(R.id.selectLocation)).perform(click())

        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withText("Please enter title")).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun shouldReturnError_noLocation() = runBlocking{
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("Reminder"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("Description"), ViewActions.closeSoftKeyboard())

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withText("Please select location")).check(matches(isDisplayed()))

        activityScenario.close()
    }


}
