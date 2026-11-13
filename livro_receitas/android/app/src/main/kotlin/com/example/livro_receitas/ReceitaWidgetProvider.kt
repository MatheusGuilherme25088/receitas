package com.example.livro_receitas

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetLaunchIntent
import es.antonborri.home_widget.HomeWidgetProvider
import org.json.JSONArray
import java.io.File
import kotlin.random.Random

class ReceitaWidgetProvider : HomeWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetData: SharedPreferences
    ) {
        val receitas = lerReceitas(widgetData)

        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.receita_widget)

            // Clique no widget abre o app na Home
            val abrirApp = HomeWidgetLaunchIntent.getActivity(context, MainActivity::class.java)
            views.setOnClickPendingIntent(R.id.widget_raiz, abrirApp)

            if (receitas.length() == 0) {
                views.setTextViewText(R.id.widget_nome, "Abra o app e toque em \"Criar Widget\"")
                views.setTextViewText(R.id.widget_tipo, "LIVRO DE RECEITAS")
                views.setImageViewResource(R.id.widget_imagem, R.drawable.widget_sem_foto)
            } else {
                val indice = sortearIndice(widgetData, widgetId, receitas.length())
                val receita = receitas.getJSONObject(indice)

                views.setTextViewText(R.id.widget_nome, receita.optString("nome"))
                views.setTextViewText(R.id.widget_tipo, receita.optString("tipo").uppercase())

                val foto = carregarFotoCircular(receita.optString("imagem"))
                if (foto != null) {
                    views.setImageViewBitmap(R.id.widget_imagem, foto)
                } else {
                    views.setImageViewResource(R.id.widget_imagem, R.drawable.widget_sem_foto)
                }
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    private fun lerReceitas(widgetData: SharedPreferences): JSONArray {
        return try {
            JSONArray(widgetData.getString("receitas_widget", "[]") ?: "[]")
        } catch (e: Exception) {
            JSONArray()
        }
    }

    /** Sorteia uma receita diferente da que foi mostrada da última vez. */
    private fun sortearIndice(widgetData: SharedPreferences, widgetId: Int, total: Int): Int {
        val chave = "widget_ultima_receita_$widgetId"
        val anterior = widgetData.getInt(chave, -1)

        var indice = Random.nextInt(total)
        if (total > 1) {
            while (indice == anterior) {
                indice = Random.nextInt(total)
            }
        }

        widgetData.edit().putInt(chave, indice).apply()
        return indice
    }

    /** Lê a imagem salva pelo Flutter, reduz o tamanho e recorta em círculo. */
    private fun carregarFotoCircular(caminho: String): Bitmap? {
        if (caminho.isEmpty() || !File(caminho).exists()) return null

        return try {
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(caminho, limites)

            var amostra = 1
            while (limites.outWidth / amostra > 400 || limites.outHeight / amostra > 400) {
                amostra *= 2
            }

            val original = BitmapFactory.decodeFile(
                caminho,
                BitmapFactory.Options().apply { inSampleSize = amostra }
            ) ?: return null

            val lado = minOf(original.width, original.height)
            val x = (original.width - lado) / 2
            val y = (original.height - lado) / 2

            val circular = Bitmap.createBitmap(lado, lado, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(circular)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            canvas.drawCircle(lado / 2f, lado / 2f, lado / 2f, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(original, Rect(x, y, x + lado, y + lado), Rect(0, 0, lado, lado), paint)

            original.recycle()
            circular
        } catch (e: Exception) {
            null
        }
    }
}
