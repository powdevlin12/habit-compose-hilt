package com.dttrn.habit_tracking.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfilePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _activeProfileId = MutableStateFlow(getSavedProfileId())
    val activeProfileId: StateFlow<Int> = _activeProfileId.asStateFlow()

    private fun getSavedProfileId(): Int = prefs.getInt(KEY_ACTIVE_PROFILE, 1)

    fun setActiveProfile(profileId: Int) {
        prefs.edit().putInt(KEY_ACTIVE_PROFILE, profileId).apply()
        _activeProfileId.value = profileId
    }

    companion object {
        private const val PREFS_NAME = "habit_journey_prefs"
        private const val KEY_ACTIVE_PROFILE = "active_profile_id"
    }
}
