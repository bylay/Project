package com.example.project

import android.content.Context
import androidx.room.*
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

// entidades
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val email: String,
    val nombre: String,
    val password: String
)

@Entity(tableName = "horarios")
data class HorariosEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val nombre: String,
    val apertura: String,
    val cierre: String,
    val dias: String,
    val activo: Boolean
)

@Entity(tableName = "historial_accesos")
data class AccesoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val emailUsuario: String,
    val tipo: String,
    val timestamp: Long,
    val estadoBarrera: String
){
    fun getFechaGrupo(): String { // Devuelve "HOY", "AYER" o la fecha
        val sdf = SimpleDateFormat("dd 'de' MMMM", Locale.getDefault())
        return sdf.format(Date(timestamp)).uppercase()
    }

    fun getHoraFormateada(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getFechaCompleta(): String {
        val sdf = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

}

// sql
@Dao
interface ParkingDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): UsuarioEntity?

    @Query("SELECT nombre FROM usuarios WHERE email = :email")
    suspend fun getUsuario(email: String): String?

    @Insert
    suspend fun insertHorario(horario: HorariosEntity)

    @Query("SELECT * FROM horarios WHERE userEmail = :email")
    suspend fun getUserHorarios(email: String): List<HorariosEntity>

    @Update
    suspend fun updateHorario(horario: HorariosEntity)

    @Insert
    suspend fun insertAccesolog(log: AccesoEntity)

    @Query("SELECT * FROM historial_accesos ORDER BY timestamp DESC")
    suspend fun getAllAccesolog(): List<AccesoEntity>

    @Query("SELECT * FROM historial_accesos ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastAccesolog(): AccesoEntity?
}

// conexion a bd
@Database(entities = [UsuarioEntity::class, HorariosEntity::class, AccesoEntity::class], version = 2)
abstract class ParkingDatabase : RoomDatabase() {
    abstract fun dao(): ParkingDao

    companion object {
        @Volatile
        private var INSTANCE: ParkingDatabase? = null

        fun getDatabase(context: Context): ParkingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParkingDatabase::class.java,
                    "smart_parking_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}