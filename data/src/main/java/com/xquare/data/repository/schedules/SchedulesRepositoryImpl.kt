package com.xquare.data.repository.schedules

import com.xquare.data.remote.datasource.SchedulesRemoteDataSource
import com.xquare.domain.entity.schedules.SchedulesEntity
import com.xquare.domain.entity.schedules.WriteSchedulesEntity
import com.xquare.domain.repository.schedules.SchedulesRepository
import javax.inject.Inject

class SchedulesRepositoryImpl @Inject constructor(
    private val schedulesRemoteDataSource: SchedulesRemoteDataSource,
): SchedulesRepository {
    override suspend fun fetchSchedules(month: Int): SchedulesEntity =
        schedulesRemoteDataSource.fetchSchedules(month)

    override suspend fun createSchedules(data: WriteSchedulesEntity) =
        schedulesRemoteDataSource.createSchedules(data)
}