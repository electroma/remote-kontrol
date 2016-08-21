import io.remotecontrol.CommandChain
import io.remotecontrol.groovy.ClosureCommand
import io.remotecontrol.groovy.server.ClosureCommandRunner
import io.remotecontrol.result.impl.DefaultResultFactory
import io.remotecontrol.server.StorageContextFactory
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
