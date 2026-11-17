package com.nexus.painel;

// Callback de progresso (0 a 100) chamado pelo serviço do Shizuku.
oneway interface IProgressCallback {
    void onProgress(int percent);
}
