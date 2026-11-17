package com.nexus.painel;

import com.nexus.painel.IProgressCallback;

// Serviço que roda no processo do Shizuku (shell) para mexer em Android/data.
interface IFileService {
    // Método reservado pelo Shizuku para encerrar o serviço.
    void destroy() = 16777114;

    // Copia a pasta [source] para dentro de [destDir], reportando o progresso.
    String transfer(String source, String destDir, IProgressCallback callback) = 1;
}
