package me.ws.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor = PrimitiveSerialDescriptor("java.math.BigDecimal", PrimitiveKind.DOUBLE)
    override fun deserialize(decoder: Decoder): BigDecimal = decoder.decodeDouble().toBigDecimal()
    override fun serialize(encoder: Encoder, value: BigDecimal) = encoder.encodeDouble(value.toDouble())
}
