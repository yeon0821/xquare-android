package com.xquare.data

import com.xquare.domain.exception.*
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import retrofit2.HttpException
import java.io.File
import java.lang.NullPointerException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun File.toMultipart(): MultipartBody.Part =
    MultipartBody.Part.createFormData(
        "file",
        this.name,
        this.asRequestBody("image/*".toMediaTypeOrNull())
    )

suspend fun <T> sendHttpRequest(
    httpRequest: suspend () -> T,
    onBadRequest: (message: String) -> Throwable = { BadRequestException() },
    onUnauthorized: (message: String) -> Throwable = { UnauthorizedException() },
    onForbidden: (message: String) -> Throwable = { ForbiddenException() },
    onNotFound: (message: String) -> Throwable = { NotFoundException() },
    onConflict: (message: String) -> Throwable = { ConflictException() },
    onServerError: (code: Int) -> Throwable = { ServerException() },
    onOtherHttpStatusCode: (code: Int, message: String) -> Throwable = { _, _ -> UnknownException() }
): T =
    try {
        httpRequest()
    } catch (e: HttpException) {
        val code = e.code()
        val message = e.message()
        throw when (code) {
            400 -> onBadRequest(message)
            401 -> onUnauthorized(message)
            403 -> onForbidden(message)
            404 -> onNotFound(message)
            409 -> onConflict(message)
            500, 501, 502, 503 -> onServerError(code)
            else -> onOtherHttpStatusCode(code, message)
        }
    } catch (e: UnknownHostException) {
        throw NoInternetException()
    } catch (e: SocketTimeoutException) {
        throw TimeoutException()
    } catch (e: NeedLoginException) {
        throw e
    } catch (e: Throwable) {
        throw UnknownException()
    }

fun <T> fetchDataWithOfflineCache(
    fetchLocalData: suspend () -> T,
    fetchRemoteData: suspend () -> T,
    checkNeedRefresh: suspend (localData: T, remoteData: T) -> Boolean =
        { localData, remoteData -> localData != remoteData },
    refreshLocalData: suspend (remoteData: T) -> Unit,
    offlineOnly: Boolean = false
) = flow {
    try {
        val localData = fetchLocalData()
        emit(localData)
        if (!offlineOnly) {
            val remoteData = fetchRemoteData()
            if (checkNeedRefresh(localData, remoteData)) {
                refreshLocalData(remoteData)
                emit(remoteData)
            }
        }
    } catch (e: NullPointerException) {
        val remoteData = fetchRemoteData()
        refreshLocalData(remoteData)
        emit(remoteData)
    }
}

fun <T> fetchPointWithOfflineCache(
    fetchLocalData: suspend () -> T,
    fetchRemoteData: suspend () -> T,
    refreshLocalData: suspend (remoteData: T) -> Unit,
    offlineOnly: Boolean = false
) = flow {
    try {
        val localData = fetchLocalData()
        if (!offlineOnly) {
            emit(fetchLocalData())
            val remoteData = fetchRemoteData()
            refreshLocalData(remoteData)
            emit(fetchLocalData())
        } else {
            emit(localData)
        }
    } catch (e: NullPointerException) {
        val remoteData = fetchRemoteData()
        refreshLocalData(remoteData)
        emit(fetchLocalData())
    }
}


fun today(): LocalDate =
    LocalDate.now()