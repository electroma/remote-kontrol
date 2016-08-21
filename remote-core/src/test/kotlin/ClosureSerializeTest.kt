import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Paths

class ClosureSerializeTest {
    @Test
    fun serializeTest() {

        val hz = { print("Zzz") }
        val classFileName = getClassName(hz)
        println(classFileName)
        val classLoader = Thread.currentThread().contextClassLoader
        val classFileResource = classLoader.getResource(classFileName)
        Files.write(Paths.get("clazz.dmp"), classFileResource.readBytes())
        Files.write(Paths.get("obj.dmp"), serialize(hz))
    }

    private fun getClassName(hz: () -> Unit) = hz.javaClass.name + ".class"

    fun serialize(obj: Any?): ByteArray {
        if (obj == null) {
            return ByteArray(0)
        }

        var baos = ByteArrayOutputStream()
        var oos = ObjectOutputStream(baos)
        oos.writeObject(obj)
        oos.close()

        return baos.toByteArray()
    }

}