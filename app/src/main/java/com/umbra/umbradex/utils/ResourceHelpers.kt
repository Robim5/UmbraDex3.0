package com.umbra.umbradex.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.umbra.umbradex.R

// Função mágica que transforma String ("standard_male1") em ID (R.drawable.standard_male1)
@Composable
fun getAvatarResourceId(assetName: String): Int {
    val context = LocalContext.current

    // Tenta encontrar o ID pelo nome
    val resourceId = context.resources.getIdentifier(
        assetName,
        "drawable",
        context.packageName
    )

    // Se encontrar (ID != 0), devolve o ID.
    // Se não encontrar (ID == 0), devolve um avatar padrão para a app não crashar.
    return if (resourceId != 0) resourceId else R.drawable.ic_launcher_foreground
}