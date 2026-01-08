package dev.lionk.lionVelocity.data

import dev.lionk.lionVelocity.LionVelocity
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


object ItemStackManager {
    private val file: Path = LionVelocity.instance.dataDirectory.resolve("servericons")
    val items = hashMapOf<String, String>()
    val itemsJson = hashMapOf<String, String>()
    fun load(){
        loadFile(file.toFile())
    }
    private fun loadFile(file: File){
        if (file.isDirectory){
            file.listFiles().forEach { fn ->
                loadFile(fn)
            }
        } else {
            if (file.extension.equals("json", true)){
                itemsJson[file.nameWithoutExtension] = Files.readString(file.toPath(), StandardCharsets.UTF_8)
            }else items[file.nameWithoutExtension] = Files.readString(file.toPath(), StandardCharsets.UTF_8)
        }
    }

}