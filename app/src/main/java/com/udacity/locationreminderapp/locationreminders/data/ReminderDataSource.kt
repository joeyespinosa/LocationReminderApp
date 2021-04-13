package com.udacity.locationreminderapp.locationreminders.data

import com.udacity.locationreminderapp.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminderapp.locationreminders.data.dto.Result

interface ReminderDataSource {
    suspend fun getReminders(): Result<List<ReminderDTO>>
    suspend fun saveReminder(reminder: ReminderDTO)
    suspend fun getReminder(id: String): Result<ReminderDTO>
    suspend fun deleteAllReminders()
}