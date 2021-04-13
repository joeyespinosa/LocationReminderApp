package com.udacity.locationreminderapp.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.locationreminderapp.R
import com.udacity.locationreminderapp.databinding.ActivityReminderDescriptionBinding
import com.udacity.locationreminderapp.locationreminders.reminderslist.ReminderDataItem

class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_REMINDER_DATA_ITEM = "EXTRA_REMINDER_DATA_ITEM"

        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_REMINDER_DATA_ITEM, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )

        intent?.let {
            binding.reminderDataItem = it.getSerializableExtra(EXTRA_REMINDER_DATA_ITEM) as ReminderDataItem
        }
    }
}
