package com.cypherchat

import android.app.Application
import com.cypherchat.core.common.DefaultDispatcherProvider
import com.cypherchat.core.common.DispatcherProvider
import com.cypherchat.core.common.Logger
import com.cypherchat.core.database.AppDatabase
import com.cypherchat.viewmodel.ChatListViewModel
import com.cypherchat.viewmodel.ConversationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class Cyph3rChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.init(debug = BuildConfig.DEBUG)

        startKoin {
            androidLogger()
            androidContext(this@Cyph3rChatApplication)
            modules(coreModule, databaseModule, viewModelModule)
        }
    }

    private val coreModule = module {
        single<DispatcherProvider> { DefaultDispatcherProvider() }
    }

    private val databaseModule = module {
        single { AppDatabase.getInstance(get()) }
        single { get<AppDatabase>().messageDao() }
        single { get<AppDatabase>().contactDao() }
    }

    private val viewModelModule = module {
        viewModel { ChatListViewModel(get(), get(), get()) }
        viewModel { (conversationId: String) ->
            ConversationViewModel(conversationId, get(), get(), get())
        }
    }
}
