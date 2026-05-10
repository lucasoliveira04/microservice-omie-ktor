package com.omie.processing

import com.omie.invoice.dto.BatchDto
import com.omie.invoice.dto.InvoiceDto
import com.omie.omie.dto.request.ContaReceberCadastro
import com.omie.omie.dto.request.IncluirContaReceberLoteParam

class OmieRequestMapper {
    fun toIncluirContaReceberLoteParam(batch: BatchDto): IncluirContaReceberLoteParam {
        return IncluirContaReceberLoteParam(
            lote = batch.numeroLote,
            contaReceberCadastro = batch.faturas.map(::toContaReceberCadastro)
        )
    }

    private fun toContaReceberCadastro(invoice: InvoiceDto): ContaReceberCadastro {
        return ContaReceberCadastro(
            codigoLancamentoIntegracao = invoice.codigoLancamentoIntegracao,
            codigoClienteFornecedor = invoice.codigoCliente,
            dataVencimento = invoice.dataVencimento,
            valorDocumento = invoice.valor,
            codigoCategoria = invoice.codigoCategoria,
            dataPrevisao = invoice.dataPrevisao,
            idContaCorrente = invoice.idContaCorrente
        )
    }
}