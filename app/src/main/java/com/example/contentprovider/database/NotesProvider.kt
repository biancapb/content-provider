package com.example.contentprovider.database

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.media.UnsupportedSchemeException
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns._ID
import androidx.annotation.RequiresApi
import com.example.contentprovider.database.NotesDatabaseHelper.Companion.TABLE_NOTES
import java.net.URI
import java.nio.channels.spi.AsynchronousChannelProvider.provider
import java.nio.channels.spi.SelectorProvider.provider

class NotesProvider : ContentProvider() {

    private lateinit var mUriMatcher: UriMatcher //responsavel pela validação de url de requisição do content provider
    private lateinit var dbHelper: NotesDatabaseHelper

    //inicializa tudo do provider
    override fun onCreate(): Boolean {
        mUriMatcher = UriMatcher(UriMatcher.NO_MATCH) //inicializa vazio
        mUriMatcher.addURI(
            AUTHORITY,
            "notes",
            NOTES
        ) //quando requisitado o endereço de url /notes ele deve trazer todas as notas criadas
        mUriMatcher.addURI(AUTHORITY, "notes/#", NOTES_BY_ID) //usada para requisição de ID

        //contexto nunca pode ser nulo
        if (context != null) {
            dbHelper = NotesDatabaseHelper(context as Context)
        }

        return true
    }

    //remove dados do provider
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        //sempre verificar a URL antes
        if (mUriMatcher.match(uri) == NOTES_BY_ID) {
            val db: SQLiteDatabase? = dbHelper.writableDatabase //habilitado para mexer
            val linesAffect = db?.delete(TABLE_NOTES, "$_ID =?", arrayOf(uri.lastPathSegment))

            db?.close()
            context?.contentResolver?.notifyChange(uri, null)

            if (linesAffect != null) {
                return linesAffect
            }
        } else {
            throw UnsupportedSchemeException("Uri invalida para exclusao")
        }
    }

    //valida uma URL
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun getType(uri: Uri): String? {
        throw UnsupportedSchemeException("Uri não implementada")
    }

    //insere dados no provider
    override fun insert(uri: Uri, values: ContentValues?): Uri? {

    }

        //seleciona os dados do provider, retorna um cursor
        override fun query(
            uri: Uri, projection: Array<String>?, selection: String?,
            selectionArgs: Array<String>?, sortOrder: String?
        ): Cursor? {
            TODO("Implement this to handle query requests from clients.")
        }

        //atualiza os dados do providades
        override fun update(
            uri: Uri, values: ContentValues?, selection: String?,
            selectionArgs: Array<String>?
        ): Int {
            TODO("Implement this to handle requests to update one or more rows.")
        }

        companion object {
            const val AUTHORITY = "com.example.contentprovider.provider"
            val BASE_URI =
                Uri.parse("content://$AUTHORITY") //requisita o content provider em qualquer aplicação
            val URI_NOTES = Uri.withAppendedPath(BASE_URI, "notes")

            //"content://com.example.contentprovider.provider//notes" -> endereço responsavel por acessar todos os dados que tiver no contenct provider

            const val NOTES = 1
            const val NOTES_BY_ID = 2
        }
    }
}