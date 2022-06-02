package de.tolstun.testcontainer.extension.impl.auth.extension.jwt

import io.jsonwebtoken.JwtBuilder


object JwtExtension {


    fun JwtBuilder.claimOfNotNull(name: String,
                                  value: Any?): JwtBuilder =

        value?.let { this.claim(name, value) } ?: this


    fun JwtBuilder.claimOfNotEmpty(name: String,
                                   value: Any?): JwtBuilder =

        value
            ?.takeIf { if(it is String) (value as String).isNotEmpty() else true }
            ?.takeIf { if(it is Map<*, *>) (value as Map<*, *>).isNotEmpty() else true }
            ?.takeIf { if(it is Collection<*>) (value as Collection<*>).isNotEmpty() else true }
            ?.let { this.claim(name, value) } ?: this


}