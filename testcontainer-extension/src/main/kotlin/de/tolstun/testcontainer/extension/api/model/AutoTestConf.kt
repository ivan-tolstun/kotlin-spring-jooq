package de.tolstun.testcontainer.extension.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AutoTestConf(@JsonProperty("globalVariables") val globalVariables: List<GlobalVariableConf>? = emptyList(),
                        @JsonProperty("restApiTests") val restApiTests: List<ItemAutoTestConf>)


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ItemAutoTestConf(@JsonProperty("testName") val testName: String,
                            @JsonProperty("method") val method: String,
                            @JsonProperty("path") var path: String,
                            @JsonProperty("queries") var queries: Map<String, Any>? = null,
                            @JsonProperty("body") var body: Map<String, Any>? = null,
                            @JsonProperty("uploadFile") var uploadFile: UploadFileConf? = null,
                            @JsonProperty("jwt") val jwt: JwtConf? = null,
                            @JsonProperty("responseStatusCode") val responseStatusCode: Int? = null,
                            @JsonProperty("responseBody") var responseBody: String? = null,
                            @JsonProperty("variablesToSave") val variablesToSave: List<VariableToSaveConf>? = null)


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GlobalVariableConf(@JsonProperty("key") val key: String,
                              @JsonProperty("value") val value: Any)


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class VariableToSaveConf(@JsonProperty("pathInJson") val pathInJson: String,
                              @JsonProperty("saveAs") val saveAs: String)


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UploadFileConf(@JsonProperty("filePath") val filePath: String,
                          @JsonProperty("controlName") val controlName: String)


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class JwtConf(@JsonProperty("body") val body:Map<String, Any>? = null,
                   @JsonProperty("expires") val expires: Long? = null)

