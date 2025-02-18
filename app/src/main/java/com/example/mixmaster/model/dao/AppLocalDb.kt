package com.example.mixmaster.model.dao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mixmaster.base.MyApplication
import com.example.mixmaster.model.Post
import com.example.mixmaster.model.User

@Database(entities = [Post::class, User::class], version = 2)
abstract class AppLocalDbRepository: RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao
}

object AppLocalDb {

    val database: AppLocalDbRepository by lazy {

        val context = MyApplication.Globals.context ?: throw IllegalStateException("Application context is missing")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "mix-master.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}