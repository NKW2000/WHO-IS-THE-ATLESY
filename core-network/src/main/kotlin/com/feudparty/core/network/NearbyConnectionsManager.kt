package com.feudparty.core.network

import kotlinx.coroutines.flow.SharedFlow

/**
 * غلاف حول Nearby Connections API. مفصول كواجهة حتى نقدر نستبدلو بـ fake
 * بالاختبارات، لأن التنفيذ الحقيقي بيحتاج Google Play Services وجهاز فعلي.
 */
interface NearbyConnectionsManager {
    val events: SharedFlow<ConnectionEvent>

    /** يستدعى من جهاز المضيف فقط */
    fun startAdvertising(serviceName: String)

    /** يستدعى من جهاز الفريق فقط */
    fun startDiscovery(serviceName: String)

    /** من جهاز الفريق ← للمضيف */
    fun sendToEndpoint(endpointId: String, message: ClientMessage)

    /** من المضيف ← لفريق واحد بالتحديد (مثلاً رسالة تخصيص الفريق) */
    fun sendToEndpoint(endpointId: String, message: HostMessage)

    /** من المضيف ← لكل الفرق المتصلة */
    fun broadcastToAll(message: HostMessage)

    fun stop()
}
