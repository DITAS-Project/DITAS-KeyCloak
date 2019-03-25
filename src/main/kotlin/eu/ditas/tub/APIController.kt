package eu.ditas.tub

import com.fasterxml.jackson.databind.ObjectMapper
import eu.ditas.tub.model.BlueprintConfig
import eu.ditas.tub.model.KeyCloakConfig
import io.javalin.Context
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import java.security.KeyPair
import java.util.*
import java.util.zip.CRC32


class APIController (val port:Int,private val admin: IKeycloakAdmin){

    private var mapper = ObjectMapper()
    private lateinit var keys: KeyPair

    init {
        initKeys()
    }

    fun init() : Javalin {
        val app:Javalin = Javalin.create().apply {
            port(port)
            exception(Exception::class.java) { e, _ -> e.printStackTrace() }
            enableCorsForAllOrigins()
            enableAutogeneratedEtags()
            enableDebugLogging()
            defaultContentType("application/json")
            enableRouteOverview("/v1/routes")
        }.start()


        app.routes {
            path("/v1") {
                post("init") {ctx -> initBlueprint(ctx)}
                path(":blueprintid"){
                    post{ctx -> applyConfiguration(ctx)}
                    put {ctx -> updateConfiguration(ctx)}
                }
                get("/keys") {
                    ctx -> getKeys(ctx)
                }
            }
        }

        return app
    }





    private fun initKeys(){
        keys = Crypto.buildKeyPair()
    }

    @Throws(Exception::class)
    fun initBlueprint(ctx: Context) {
        val config = mapper.readValue(ctx.bodyAsBytes(),BlueprintConfig::class.java)

        val credentials = admin.initizeRelam(config)

        ctx.status(201).result("<todo>")

        //TODO return service account credentials
    }

    @Throws(Exception::class)
    fun applyConfiguration(ctx: Context) {
        val config = getKeycloakConfigFrom(ctx)
        admin.applyConfig(config)
        ctx.status(200).result("applied config successfully")
   }


    @Throws(Exception::class)
    fun updateConfiguration(ctx: Context) {
        val config = getKeycloakConfigFrom(ctx)
        //TODO: same behavior as before using the file. however it is possible that user etc are already present
        print("got config $config")

    }

    @Throws(Exception::class)
    fun getKeys(ctx: Context) {
        val key = keys.public
        ctx.json(SingingKey(key.encoded)).status(200)
    }

    @Throws(Exception::class)
    private fun getKeycloakConfigFrom(ctx:Context): KeyCloakConfig {
        val body = ctx.body()
        val configData = Crypto.decrypt(keys.private, Base64.getDecoder().decode(body))
        val config = mapper.readValue(configData, KeyCloakConfig::class.java)
        val id = ctx.pathParam("blueprintid")
        config.blueprintID = id
        return config
    }

}

data class SingingKey(private val keyBytes:ByteArray) {
    val algorithm = Crypto.CIPHER_METHOD
    val key = Base64.getEncoder().encodeToString(keyBytes)
    val crc = crc(keyBytes)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SingingKey

        if (algorithm != other.algorithm) return false
        if(crc != other.crc) return false

        return true
    }

    fun crc(key:ByteArray) : Long{
        val crc = CRC32()
        with (crc) {
            update(key)
        }
        return crc.value
    }

    override fun hashCode(): Int {
        var result = algorithm.hashCode()
        result = 31 * result + keyBytes.contentHashCode()
        result = 31 * result + crc.hashCode()
        return result
    }


}