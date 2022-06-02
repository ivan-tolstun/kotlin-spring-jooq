package de.tolstun.testcontainer.extension.impl.behavior

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import de.tolstun.testcontainer.extension.api.model.AutoTestConf
import java.io.File
import java.io.FileNotFoundException


object AutoTestBehavior {


    fun File.readAsAutoTestConf(): AutoTestConf =

        this.takeIf { it.isFile }
            ?.let {
                val mapper = ObjectMapper(YAMLFactory())
                mapper.findAndRegisterModules()
                mapper.readValue(it, AutoTestConf::class.java)
            }
            ?: throw FileNotFoundException("Configuration file for autotest not found")


}