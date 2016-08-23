import io.remotekontrol.CommandChain
import io.remotekontrol.kotlin.ClosureCommand
import io.remotekontrol.kotlin.server.ClosureCommandRunner
import io.remotekontrol.result.impl.DefaultResultFactory
import io.remotekontrol.server.StorageContextFactory
import org.junit.Test
import java.nio.file.Files.readAllBytes
import java.nio.file.Paths

class ClosureDeserializeTest {

    @Test
    fun deserializeTest() {
        val clazz = readAllBytes(Paths.get(classLoader().getResource("clazz.dmp").toURI()))
        val obj = readAllBytes(Paths.get(classLoader().getResource("obj.dmp").toURI()))
        val closureCommand = ClosureCommand(obj, clazz, emptyList())
        val classLoader = Thread.currentThread().contextClassLoader
        val closureCommandRunner = ClosureCommandRunner(classLoader, StorageContextFactory.withEmptyStorage(), DefaultResultFactory())
        closureCommandRunner.run(CommandChain(ClosureCommand::class.java, listOf(closureCommand)))
    }

    private fun classLoader() = this.javaClass.classLoader
}
