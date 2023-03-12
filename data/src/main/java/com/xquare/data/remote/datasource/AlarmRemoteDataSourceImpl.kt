package com.xquare.data.remote.datasource

import com.xquare.data.remote.api.NotificationApi
import com.xquare.data.remote.response.notification.toEntity
import com.xquare.domain.entity.notification.AlarmEntity
import javax.inject.Inject

class AlarmRemoteDataSourceImpl @Inject constructor(
    private val notificationApi: NotificationApi,
): AlarmRemoteDataSource {
    override suspend fun fetchAlarm(): AlarmEntity =
        notificationApi.fetchAlarm().toEntity()
}