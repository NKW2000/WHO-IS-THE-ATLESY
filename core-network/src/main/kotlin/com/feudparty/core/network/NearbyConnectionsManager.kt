package com.feudparty.core.network

import kotlinx.coroutines.flow.SharedFlow

/**
 * غلاف حول Nearby Connections API. مفصول كواجهة حتى نقدر نستبدلو بـ fake
 * بالاختبارات، لأن التنفيذ الحقيقي بيحتاج Google Play Services وجهاز فعلي.
 */
interface NearbyConnectionsManager {
    val events: SharedFlow<ConnectionEvent>

    /**
     * يستدعى من جهاز المضيف فقط. [displayName] هو اسم الغرفة اللي بيشوفه
     * اللاعبين بلستة الغرف.
     */
    fun startAdvertising(serviceName: String, displayName: String? = null)

    /**
     * يستدعى من جهاز اللاعب فقط. البحث بيرجّع غرف عبر
     * [ConnectionEvent.RoomFound]، والاتصال ما بيصير إلا بـ [connectTo].
     */
    fun startDiscovery(serviceName: String)

    /** اللاعب اختار غرفة — هون بس منطلب الاتصال. */
    fun connectTo(endpointId: String)

    /** من جهاز الفريق ← للمضيف */
    fun sendToEndpoint(endpointId: String, message: ClientMessage)

    /** من المضيف ← لفريق واحد بالتحديد (مثلاً رسالة تخصيص الفريق) */
    fun sendToEndpoint(endpointId: String, message: HostMessage)

    /** من المضيف ← لكل الفرق المتصلة */
    fun broadcastToAll(message: HostMessage)

    fun stop()
}
