package fr.formation.hydratation.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import fr.formation.hydratation.MainActivity
import fr.formation.hydratation.R

object NotificationsRappel {
    const val CANAL_RAPPELS = "rappels"
    const val ID_RAPPEL = 1001
    const val ACTION_CEST_FAIT = "fr.formation.hydratation.action.CEST_FAIT"

    /** Étape 1 : minSdk 34, les canaux existent toujours (pas de test de version). */
    fun creerCanal(context: Context) {
        val canal = NotificationChannel(
            CANAL_RAPPELS,
            context.getString(R.string.canal_rappels_nom),
            NotificationManager.IMPORTANCE_DEFAULT,          // son, sans bandeau intrusif
        ).apply {
            description = context.getString(R.string.canal_rappels_description)
        }
        context.getSystemService<NotificationManager>()?.createNotificationChannel(canal)
    }

    /** Étape 3 : notification BigText ; clic → ouvre l'app ; action « C'est fait ». */
    fun construire(context: Context): Notification {
        val ouvrirApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val clic = PendingIntent.getActivity(
            context, 0, ouvrirApp,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        // Étape 6 : broadcast EXPLICITE vers le receiver de l'application
        val cestFait = PendingIntent.getBroadcast(
            context, 1,
            Intent(context, CestFaitReceiver::class.java).setAction(ACTION_CEST_FAIT),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(context, CANAL_RAPPELS)
            .setSmallIcon(R.drawable.ic_goutte)                       // monochrome, obligatoire
            .setContentTitle(context.getString(R.string.rappel_titre))
            .setContentText(context.getString(R.string.rappel_texte))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.rappel_texte_long)),
            )
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(clic)
            .setAutoCancel(true)                                      // disparaît au clic
            .setOnlyAlertOnce(true)                                   // pas de son à la mise à jour
            .addAction(R.drawable.ic_check, context.getString(R.string.action_cest_fait), cestFait)
            .build()
    }

    /** @return true si la notification a pu être affichée. */
    fun afficher(context: Context): Boolean {
        // Sans la permission, notify() serait ignoré silencieusement : on n'insiste pas
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return false
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false
        manager.notify(ID_RAPPEL, construire(context))               // même id = mise à jour
        return true
    }

    fun retirer(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID_RAPPEL)
    }
}
