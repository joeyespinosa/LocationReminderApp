package com.udacity.locationreminderapp

import android.app.Application
import com.udacity.locationreminderapp.locationreminders.data.ReminderDataSource
import com.udacity.locationreminderapp.locationreminders.data.local.LocalDB
import com.udacity.locationreminderapp.locationreminders.data.local.RemindersLocalRepository
import com.udacity.locationreminderapp.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.locationreminderapp.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class LocationReminderApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }

            single {
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(this@LocationReminderApp) }
        }

        startKoin {
            androidContext(this@LocationReminderApp)
            modules(listOf(myModule))
        }
    }
}