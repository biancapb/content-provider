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
            //operação de delete
            val db: SQLiteDatabase? = dbHelper.writableDatabase //habilitado para mexer
            val linesAffect: Int? = db?.delete(TABLE_NOTES, "$_ID =?", arrayOf(uri.lastPathSegment))

            db?.close()
            //notifica sobre alteração no content provider
            context?.contentResolver?.notifyChange(uri, null)

             return linesAffect!!


        } else {
            throw UnsupportedSchemeException("Uri invalida para exclusao")
        }
    }

    //valida uma URL, serve para manipulação de arquivos
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun getType(uri: Uri): String? {
        throw UnsupportedSchemeException("Uri não implementada")
    }

    //insere dados no provider
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        //se for feita requisição de um valor
        if (mUriMatcher.match(uri) == NOTES) {
            val db: SQLiteDatabase? = dbHelper.writableDatabase //habilitado para mexer
            val id = db?.insert(TABLE_NOTES, null, values)
            val insertUri =
                Uri.withAppendedPath(BASE_URI, id.toString()) //declada o uri e o insere no sqLite

            db?.close()

            context?.contentResolver?.notifyChange(uri, null) //notifica sobre a mudança
            return insertUri

        }else{
            throw UnsupportedSchemeException("Uri inválida para inserção")
        }

    }

        //seleciona os dados do provider, retorna um cursor
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        override fun query(
            uri: Uri, projection: Array<String>?, selection: String?,
            selectionArgs: Array<String>?, sortOrder: String?
        ): Cursor? {
            return when {
                mUriMatcher.match(uri) == NOTES -> {
                    val db: SQLiteDatabase = dbHelper.writableDatabase
                    //os dados são passados para um cursos
                    val cursor = db.query(
                        TABLE_NOTES,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                    )

                    //notifica que recebeu os dados
                    cursor.setNotificationUri(context?.contentResolver, uri)
                    cursor

                }
                mUriMatcher.match(uri) == NOTES_BY_ID -> {
                    val db: SQLiteDatabase = dbHelper.writableDatabase
                    val cursor = db.query(
                        TABLE_NOTES,
                        projection,
                        "$_ID = ?",
                        arrayOf(uri.lastPathSegment),
                        null,
                        null,
                        sortOrder
                    )
                    cursor.setNotificationUri(context?.contentResolver, uri)
                    cursor
                }
                else -> {
                    throw UnsupportedSchemeException("Uri não implementada")
                }
            }
        }

        //atualiza os dados do providades
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        override fun update(
            uri: Uri, values: ContentValues?, selection: String?,
            selectionArgs: Array<String>?
        ): Int {
            if (mUriMatcher.match(uri) == NOTES_BY_ID) {
                val db: SQLiteDatabase = dbHelper.writableDatabase
                val linesAffect = db.update(TABLE_NOTES, values, "$_ID = ?", arrayOf(uri.lastPathSegment))

                db.close()

                context?.contentResolver?.notifyChange(uri, null)
                return linesAffect
            }  else {
                throw UnsupportedSchemeException("Uri não implementada")
            }
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
