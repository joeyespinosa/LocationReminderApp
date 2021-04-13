package com.udacity.locationreminderapp.locationreminders.reminderslist

import com.udacity.locationreminderapp.R
import com.udacity.locationreminderapp.base.BaseRecyclerViewAdapter

class RemindersListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder
}