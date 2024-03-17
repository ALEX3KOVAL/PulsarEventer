package ru.alex3koval.eventer.app

import com.zaxxer.hikari.HikariDataSource
import org.apache.pulsar.client.api.PulsarClient
import org.jetbrains.exposed.sql.Database
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.alex3koval.eventer.app.contract.EventPersister
import ru.alex3koval.eventer.app.repository.EventRepository
import ru.alex3koval.eventer.storage.EventRepositoryImpl
import ru.alex3koval.eventer.storage.persister.EventPersisterImpl
import ru.coderocket.sza.configure.settings.SystemSettings
import javax.sql.DataSource

object EventerDI {
    lateinit var di: KoinApplication

    private val KoinApplication.persistersModule: Module by lazy {
        module {
            singleOf<EventPersister>(::EventPersisterImpl)
        }
    }

    private val KoinApplication.repositoriesModule: Module by lazy {
        module {
            singleOf<EventRepository>(::EventRepositoryImpl)
        }
    }

    private val KoinApplication.systemSettingsModule: Module by lazy {
        module {
            single<SystemSettings> { SystemSettings.read() }
        }
    }

    private val KoinApplication.pulsarClientModule: Module by lazy {
        module {
            factory<PulsarClient> {
                val env: SystemSettings = get()

                PulsarClient
                    .builder()
                    .listenerName(env.pulsar.listener)
                    .serviceUrl(env.pulsar.url)
                    .build()
            }
        }
    }

    private val KoinApplication.dataSourceModule: Module by lazy {
        module {
            single<DataSource>(named("pgsql")) {
                val env: SystemSettings = get()

                HikariDataSource().also {
                    it.jdbcUrl = env.db.jdbcURL
                    it.username = env.db.login
                    it.password = env.db.password
                    it.maximumPoolSize = 1
                }
            }
        }
    }

    private val KoinApplication.databaseModule: Module by lazy {
        module {
            single<Database>(createdAtStart = true) {
                logger.info("#####   Connect to PGSQL")
                Database.connect(get<DataSource>(named("pgsql")))
            }
        }
    }

    fun init() {
        di = startKoin {
            systemSettingsModule
            dataSourceModule
            databaseModule
            pulsarClientModule
            persistersModule
            repositoriesModule
        }
    }

    inline fun<reified T : Any> get(): Result<T> = runCatching {
        if (!::di.isInitialized) {
            throw UninitializedPropertyAccessException("Переменная di не проинициализирована")
        }

        di.koin.get<T>()
    }
}