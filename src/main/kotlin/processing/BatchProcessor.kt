package com.omie.processing

import com.omie.omie.OmieClient
import com.omie.invoice.dto.BatchDto
import org.slf4j.LoggerFactory

class BatchProcessor(
    private val mapper: OmieRequestMapper,
    private val omieClient: OmieClient,
) {
    private val logger = LoggerFactory.getLogger(BatchProcessor::class.java)

    suspend fun process(batch: BatchDto) {
        logger.info(
            "Processando lote: loteId={}, correlationId={}, faturas={}",
            batch.loteId, batch.correlationId, batch.faturas.size
        )

        val param = mapper.toIncluirContaReceberLoteParam(batch)

        val response = try {
            omieClient.incluirContaReceberLote(param)
        } catch (e: Exception) {
            logger.error(
                "Falha na chamada OMIE: loteId={}, motivo={}",
                batch.loteId, e.message, e
            )
            throw e
        }

        // Análise do lote inteiro
        if (response.codigoStatus != "0") {
            logger.warn(
                "Lote rejeitado pela OMIE: loteId={}, codigoStatus={}, descricao={}",
                batch.loteId, response.codigoStatus, response.descricaoStatus
            )
            return
        }

        val sucessos = response.statusLote.filter { it.codigoStatus == "0" }
        val erros = response.statusLote.filter { it.codigoStatus != "0" }

        logger.info(
            "Lote processado: loteId={}, sucessos={}, erros={}",
            batch.loteId, sucessos.size, erros.size
        )

        sucessos.forEach { item ->
            logger.info(
                "  ✓ {} → codigoOmie={}",
                item.codigoLancamentoIntegracao, item.codigoLancamentoOmie
            )
        }

        erros.forEach { item ->
            logger.warn(
                "  ✗ {} → status={}, descricao={}",
                item.codigoLancamentoIntegracao, item.codigoStatus, item.descricaoStatus
            )
        }
    }
}