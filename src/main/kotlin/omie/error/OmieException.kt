package com.omie.omie.error

import com.omie.omie.dto.response.OmieErrorResponse

class OmieException(
    val errorResponse: OmieErrorResponse,
    val tipo: OmieErroTipo = OmieErroTipo.fromFaultcode(errorResponse.faultstring)
) : RuntimeException(errorResponse.faultstring) {
}