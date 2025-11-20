package co.edu.unipiloto.myapplication.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {

    fun savePdfToExternalFile(context: Context, inputStream: InputStream, fileName: String): File {
        val pdfFile = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(pdfFile).use { output ->
            inputStream.copyTo(output)
        }
        return pdfFile
    }

    fun openPdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        context.startActivity(intent)
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

}
