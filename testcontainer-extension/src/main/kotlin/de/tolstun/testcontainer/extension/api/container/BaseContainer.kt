package de.tolstun.testcontainer.extension.api.container

import org.testcontainers.containers.BindMode
import org.testcontainers.containers.Container
import org.testcontainers.containers.GenericContainer
import org.testcontainers.shaded.com.google.common.io.Resources
import java.io.File
import java.io.FileNotFoundException


interface BaseContainer<SELF : GenericContainer<SELF>> : Container<SELF> {


    fun findSourceFile(filePathFromSourcesFolder: String) =

        File(Resources.getResource(filePathFromSourcesFolder).path)
            .takeIf { it.exists() }
            ?: throw FileNotFoundException("File not found in source folder")


    fun withSharedFiles(filesToBind: Map<String, String>? = emptyMap(),
                        bindMode: BindMode = BindMode.READ_WRITE): SELF {

        filesToBind
            ?.takeIf { it.isNotEmpty() }
            ?.map { (fromPath, toPath) -> findSourceFile(fromPath) to toPath }
            ?.forEach{ (file, toPath) -> this.withFileSystemBind(file.absolutePath, toPath, bindMode) }

        return self() as SELF
    }


}