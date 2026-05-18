package com.dttrn.habit_tracking.data.repository

import com.dttrn.habit_tracking.data.db.dao.ProfileDao
import com.dttrn.habit_tracking.data.db.entity.ProfileEntity
import com.dttrn.habit_tracking.data.preferences.ProfilePreferences
import com.dttrn.habit_tracking.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao,
    private val profilePreferences: ProfilePreferences
) {
    val activeProfileId: Flow<Int> = profilePreferences.activeProfileId

    fun getAllProfiles(): Flow<List<Profile>> =
        profileDao.getAllProfiles().map { list -> list.map { it.toDomain() } }

    suspend fun getProfileById(id: Int): Profile? =
        profileDao.getProfileById(id)?.toDomain()

    suspend fun insertProfile(profile: Profile): Long =
        profileDao.insertProfile(profile.toEntity())

    suspend fun updateProfile(profile: Profile) =
        profileDao.updateProfile(profile.toEntity())

    suspend fun deleteProfile(id: Int) =
        profileDao.deleteProfile(id)

    suspend fun count(): Int = profileDao.count()

    fun setActiveProfile(profileId: Int) =
        profilePreferences.setActiveProfile(profileId)

    fun getActiveProfileId(): Int = profilePreferences.activeProfileId.value

    /** Đảm bảo profile mặc định tồn tại khi app mới cài */
    suspend fun ensureDefaultProfile() {
        if (profileDao.count() == 0) {
            profileDao.insertProfile(
                ProfileEntity(id = 1, name = "Tôi", avatarEmoji = "🙂")
            )
        }
    }

    private fun ProfileEntity.toDomain() = Profile(
        id = id, name = name, avatarEmoji = avatarEmoji, createdAt = createdAt
    )

    private fun Profile.toEntity() = ProfileEntity(
        id = id, name = name, avatarEmoji = avatarEmoji, createdAt = createdAt
    )
}
