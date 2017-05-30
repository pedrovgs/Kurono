package com.github.pedrovgs.kuronometer.free.interpreter.api

import com.github.pedrovgs.kuronometer.KuronometerResults.{
  ConnectionError,
  KuronometerResult,
  UnknownError
}
import com.github.pedrovgs.kuronometer.free.domain.{BuildExecution, Platform}
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write
import net.liftweb.json.ext.EnumNameSerializer

import scala.util.Try
import scalaj.http.Http

class KuronometerApiClient {

  private implicit val formats = DefaultFormats + new EnumNameSerializer(
    Platform)

  def report(buildExecution: BuildExecution)(
      implicit apiClientConfig: KuronometerApiClientConfig)
    : KuronometerResult[BuildExecution] = {
    val json = write(buildExecution)
    sendPostRequest(buildExecution, json, "/buildExecution")
  }

  private def sendPostRequest(
      buildExecution: BuildExecution,
      body: String,
      path: String)(implicit apiClientConfig: KuronometerApiClientConfig)
    : KuronometerResult[BuildExecution] = {
    Try(
      Http(composeUrl(path))
        .headers(KuronometerApiClientConfig.headers)
        .postData(body)
        .asString)
      .map(response =>
        if (response.isSuccess) Right(buildExecution) else Left(UnknownError))
      .toOption
      .getOrElse(Left(ConnectionError))
  }

  private def composeUrl(path: String)(
      implicit apiClientConfig: KuronometerApiClientConfig): String =
    apiClientConfig.scheme + "://" + apiClientConfig.host + ":" + apiClientConfig.port + path
}
