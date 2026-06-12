package com.example.ui.components

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

object PdfPrintUtil {
    fun printHtml(context: Context, htmlContent: String, jobName: String) {
        try {
            val webView = WebView(context)
            // Enable JavaScript if needed, but simple styling usually doesn't require it
            webView.settings.javaScriptEnabled = false
            
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    if (printManager == null) {
                        Toast.makeText(context, "Layanan cetak tidak tersedia pada perangkat ini", Toast.LENGTH_LONG).show()
                        return
                    }
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    val attributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("id", "print", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                    
                    try {
                        printManager.print(jobName, printAdapter, attributes)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Kesalahan printing: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal meluncurkan print engine: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
